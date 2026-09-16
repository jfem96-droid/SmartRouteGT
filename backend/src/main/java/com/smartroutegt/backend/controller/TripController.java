package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopVisitRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.service.StopVisitService;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;
    private final DriverRouteAssignmentRepository assignmentRepository;
    private final RouteStopRepository routeStopRepository;
    private final StopVisitRepository stopVisitRepository;
    private final BusLocationRepository locationRepository;
    private final StopVisitService stopVisitService;

    public TripController(
            TripRepository tripRepository,
            DriverRouteAssignmentRepository assignmentRepository,
            RouteStopRepository routeStopRepository,
            StopVisitRepository stopVisitRepository,
            BusLocationRepository locationRepository,
            StopVisitService stopVisitService) {

        this.tripRepository = tripRepository;
        this.assignmentRepository = assignmentRepository;
        this.routeStopRepository = routeStopRepository;
        this.stopVisitRepository = stopVisitRepository;
        this.locationRepository = locationRepository;
        this.stopVisitService = stopVisitService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> iniciarViaje(@RequestParam Long assignmentId) {

        DriverRouteAssignment assignment = assignmentRepository.findById(assignmentId).orElse(null);

        if (assignment == null) {
            return ResponseEntity.badRequest()
                    .body("La asignación no existe");
        }

        if (!assignment.isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La asignación está inactiva");
        }

        if (!assignment.getDriver().isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El piloto está inactivo");
        }

        if (!assignment.getBus().isActiva()) {
            return ResponseEntity.badRequest()
                    .body("El bus está inactivo");
        }

        if (!assignment.getRoute().isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La ruta está inactiva");
        }
        if (tripRepository.existsByDriverIdAndActivoTrue(
                assignment.getDriver().getId())) {

            return ResponseEntity.badRequest()
                    .body("El piloto ya tiene un viaje activo");
        }

        Trip trip = new Trip(
                assignment.getDriver(),
                assignment.getBus(),
                assignment.getRoute());

        trip.setFechaInicio(LocalDateTime.now());
        trip.setActivo(true);

        Trip viajeGuardado = tripRepository.save(trip);

        return ResponseEntity.ok(viajeGuardado);
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<?> finalizarViaje(@PathVariable Long id) {

        Trip trip = tripRepository.findById(id).orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje no existe");
        }

        if (!trip.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El viaje ya está finalizado");
        }

        trip.setFechaFin(LocalDateTime.now());
        trip.setActivo(false);

        Trip viajeFinalizado = tripRepository.save(trip);

        return ResponseEntity.ok(viajeFinalizado);
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<Trip> obtenerViajeActivoPorPiloto(
            @PathVariable Long driverId) {

        return tripRepository.findByDriverIdAndActivoTrue(driverId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{tripId}/next-stop")
    public ResponseEntity<?> obtenerProximaParada(
            @PathVariable Long tripId) {

        Trip trip = tripRepository.findById(tripId).orElse(null);

        if (trip == null) {
            return ResponseEntity.notFound().build();
        }

        List<RouteStop> paradasRuta = routeStopRepository
                .findByRouteIdOrderByOrdenAsc(
                        trip.getRoute().getId());

        for (RouteStop routeStop : paradasRuta) {

            boolean cumplida = stopVisitRepository
                    .existsByTripIdAndStopIdAndCumplidaTrue(
                            tripId,
                            routeStop.getStop().getId());

            if (!cumplida) {

                Map<String, Object> respuesta = new LinkedHashMap<>();

                respuesta.put("tripId", trip.getId());
                respuesta.put("stopId", routeStop.getStop().getId());
                respuesta.put("parada", routeStop.getStop().getNombre());
                respuesta.put("orden", routeStop.getOrden());
                respuesta.put("latitud", routeStop.getStop().getLatitud());
                respuesta.put("longitud", routeStop.getStop().getLongitud());
                respuesta.put(
                        "radioPermitido",
                        routeStop.getStop().getRadioPermitido());

                BusLocation ultimaUbicacion = locationRepository
                        .findFirstByBusIdOrderByFechaHoraDesc(
                                trip.getBus().getId())
                        .orElse(null);

                if (ultimaUbicacion != null) {

                    double distanciaMetros = stopVisitService
                            .calcularDistanciaHaversine(
                                    ultimaUbicacion.getLatitud(),
                                    ultimaUbicacion.getLongitud(),
                                    routeStop.getStop().getLatitud(),
                                    routeStop.getStop().getLongitud());

                    respuesta.put("distanciaMetros", distanciaMetros);

                } else {
                    respuesta.put("distanciaMetros", null);
                }

                return ResponseEntity.ok(respuesta);
            }
        }

        return ResponseEntity.ok(
                "No hay paradas pendientes");
    }
}