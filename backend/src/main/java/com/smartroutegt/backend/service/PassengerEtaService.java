
package com.smartroutegt.backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.smartroutegt.backend.dto.routing.RouteGeometryResponse;
import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopVisitRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.service.traffic.TrafficEstimate;
import com.smartroutegt.backend.service.traffic.TrafficService;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PassengerEtaService {

    private final TripRepository tripRepository;
    private final RouteStopRepository routeStopRepository;
    private final StopVisitRepository stopVisitRepository;
    private final BusLocationRepository locationRepository;
    private final StopVisitService stopVisitService;
    private final RouteGeometryService routeGeometryService;
    private final TrafficService trafficService;

    public PassengerEtaService(
            TripRepository tripRepository,
            RouteStopRepository routeStopRepository,
            StopVisitRepository stopVisitRepository,
            BusLocationRepository locationRepository,
            StopVisitService stopVisitService,
            RouteGeometryService routeGeometryService,
            TrafficService trafficService) {

        this.tripRepository = tripRepository;
        this.routeStopRepository = routeStopRepository;
        this.stopVisitRepository = stopVisitRepository;
        this.locationRepository = locationRepository;
        this.stopVisitService = stopVisitService;
        this.routeGeometryService = routeGeometryService;
        this.trafficService = trafficService;
    }

    public Map<String, Object> calculate(
            Long tripId,
            Long targetStopId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException("El viaje no existe"));

        List<RouteStop> routeStops =
                routeStopRepository.findByRouteIdOrderByOrdenAsc(
                        trip.getRoute().getId());

        RouteStop targetStop = routeStops.stream()
                .filter(rs ->
                        rs.getStop().getId().equals(targetStopId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La parada no pertenece a la ruta"));

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("tripId", tripId);
        response.put("routeId", trip.getRoute().getId());
        response.put("stopId", targetStopId);
        response.put("parada", targetStop.getStop().getNombre());

        boolean targetAlreadyVisited =
                stopVisitRepository
                        .existsByTripIdAndStopIdAndCumplidaTrue(
                                tripId,
                                targetStopId);

        if (targetAlreadyVisited) {
            response.put("estado", "BUS_YA_PASO");
            response.put("busYaPaso", true);
            response.put("etaMinutos", null);
            response.put("distanciaMetros", 0.0);
            return response;
        }

        BusLocation latestLocation =
                locationRepository
                        .findFirstByTripIdOrderByFechaHoraDesc(tripId)
                        .orElse(null);

        if (latestLocation == null) {
            response.put("estado", "SIN_GPS");
            response.put("busYaPaso", false);
            response.put("etaMinutos", null);
            response.put("distanciaMetros", null);
            return response;
        }

long gpsAgeSeconds = Duration.between(
        latestLocation.getFechaHora(),
        LocalDateTime.now()
).getSeconds();

response.put("antiguedadGpsSegundos", gpsAgeSeconds);

final long MAX_GPS_AGE_SECONDS = 120;

if (gpsAgeSeconds > MAX_GPS_AGE_SECONDS) {
    response.put("estado", "GPS_DESACTUALIZADO");
    response.put("busYaPaso", false);
    response.put("etaMinutos", null);
    response.put("distanciaMetros", null);
    response.put("velocidadActualKmh", round(latestLocation.getVelocidad()));
    response.put("ultimaActualizacion", latestLocation.getFechaHora());
    return response;
}


        RouteStop nextPendingStop = null;

        for (RouteStop routeStop : routeStops) {

            boolean visited =
                    stopVisitRepository
                            .existsByTripIdAndStopIdAndCumplidaTrue(
                                    tripId,
                                    routeStop.getStop().getId());

            if (!visited) {
                nextPendingStop = routeStop;
                break;
            }
        }

        if (nextPendingStop == null) {
            response.put("estado", "VIAJE_COMPLETADO");
            response.put("busYaPaso", true);
            response.put("etaMinutos", null);
            response.put("distanciaMetros", 0.0);
            return response;
        }

        if (nextPendingStop.getOrden() > targetStop.getOrden()) {
            response.put("estado", "BUS_YA_PASO");
            response.put("busYaPaso", true);
            response.put("etaMinutos", null);
            response.put("distanciaMetros", 0.0);
            return response;
        }

        double distanceToNextStop =
                stopVisitService.calcularDistanciaHaversine(
                        latestLocation.getLatitud(),
                        latestLocation.getLongitud(),
                        nextPendingStop.getStop().getLatitud(),
                        nextPendingStop.getStop().getLongitud());

        double routeDistanceAfterNextStop = 0.0;
        double valhallaDurationSeconds = 0.0;

        if (!nextPendingStop.getStop().getId().equals(targetStopId)) {

            RouteGeometryResponse geometry =
                    routeGeometryService.getRouteGeometry(
                            trip.getRoute().getId(),
                            nextPendingStop.getStop().getId(),
                            targetStopId);

            routeDistanceAfterNextStop =
                    geometry.getDistanceMeters();

            valhallaDurationSeconds =
                    geometry.getDurationSeconds();
        }

        double totalDistance =
                distanceToNextStop
                        + routeDistanceAfterNextStop;

        TrafficEstimate traffic =
                trafficService.estimate(
                        tripId,
                        latestLocation.getVelocidad());

        double estimatedSpeedKmh =
                traffic.velocidadEstimadaKmh();

        double estimatedSpeedMetersSecond =
                estimatedSpeedKmh / 3.6;

        long etaSeconds = Math.round(
                totalDistance / estimatedSpeedMetersSecond);

        long etaMinutes =
                (long) Math.ceil(etaSeconds / 60.0);

        response.put("estado", "EN_CAMINO");
        response.put("busYaPaso", false);

        response.put(
                "distanciaMetros",
                round(totalDistance));

        response.put(
                "distanciaGpsAProximaParadaMetros",
                round(distanceToNextStop));

        response.put(
                "distanciaVialRestanteMetros",
                round(routeDistanceAfterNextStop));

        response.put(
                "velocidadActualKmh",
                round(latestLocation.getVelocidad()));

        response.put(
                "velocidadEstimadaKmh",
                traffic.velocidadEstimadaKmh());

        response.put("etaSegundos", etaSeconds);
        response.put("etaMinutos", etaMinutes);

        response.put("trafico", traffic.estado());
        response.put(
                "factorTrafico",
                traffic.factorTrafico());

        response.put(
                "proveedorTrafico",
                traffic.proveedor());

        response.put(
                "proveedorRuta",
                "VALHALLA_OSM");

        response.put(
                "duracionBaseValhallaSegundos",
                round(valhallaDurationSeconds));

        response.put(
                "ultimaActualizacion",
                latestLocation.getFechaHora());

        return response;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
