package com.smartroutegt.backend.service;

import com.smartroutegt.backend.dto.routing.RouteGeometryPoint;
import com.smartroutegt.backend.dto.routing.RouteGeometryResponse;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.RouteStopRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteGeometryService {

    private static final String VALHALLA_URL =
            "https://valhalla1.openstreetmap.de/route";

    private final RouteItineraryService itineraryService;
    private final RestClient restClient;

    public RouteGeometryService(
            RouteItineraryService itineraryService) {

        this.itineraryService = itineraryService;

        this.restClient = RestClient.builder()
                .defaultHeader(
                        "X-Client-Id",
                        "SmartRouteGT-Development"
                )
                .build();
    }

    public RouteGeometryResponse getRouteGeometry(Long routeId) {
        return getRouteGeometry(routeId, null, null);
    }

    public RouteGeometryResponse getRouteGeometry(
            Long routeId,
            Long originStopId,
            Long destinationStopId) {

        return getRouteGeometry(routeId, originStopId, destinationStopId, null);
    }

    public RouteGeometryResponse getRouteGeometry(Long routeId, Long originStopId,
                                                   Long destinationStopId, Trip.Sentido direction) {
        List<Stop> routeStops = itineraryService.resolve(routeId, originStopId, destinationStopId, direction).stops();

        List<Map<String, Object>> locations = new ArrayList<>();

        for (Stop routeStop : routeStops) {

            Map<String, Object> location =
                    new LinkedHashMap<>();

            location.put(
                    "lat",
                    routeStop.getLatitud()
            );

            location.put(
                    "lon",
                    routeStop.getLongitud()
            );

            location.put("type", "break");

            locations.add(location);
        }

        Map<String, Object> request =
                new LinkedHashMap<>();

        request.put("locations", locations);
        request.put("costing", "auto");
        request.put("units", "kilometers");

        Map<?, ?> response;
        try {
            response = restClient.post().uri(VALHALLA_URL)
                .contentType(MediaType.APPLICATION_JSON).body(request)
                .retrieve().body(Map.class);
        } catch (RestClientException error) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "El proveedor de mapas no pudo calcular el trazado de este recorrido", error);
        }

        if (response == null) {
            throw new IllegalStateException(
                    "Valhalla no devolvió respuesta"
            );
        }

        Object tripObject = response.get("trip");

        if (!(tripObject instanceof Map<?, ?> trip)) {
            throw new IllegalStateException(
                    "Valhalla no devolvió un viaje válido"
            );
        }

        double distanceMeters = 0.0;
        double durationSeconds = 0.0;

        Object summaryObject = trip.get("summary");

        if (summaryObject instanceof Map<?, ?> summary) {

            distanceMeters =
                    numberValue(summary.get("length")) * 1000.0;

            durationSeconds =
                    numberValue(summary.get("time"));
        }

        List<RouteGeometryPoint> points =
                new ArrayList<>();

        Object legsObject = trip.get("legs");

        if (!(legsObject instanceof List<?> legs)) {
            throw new IllegalStateException(
                    "Valhalla no devolvió tramos de ruta"
            );
        }

        for (Object legObject : legs) {

            if (!(legObject instanceof Map<?, ?> leg)) {
                continue;
            }

            Object shapeObject = leg.get("shape");

            if (!(shapeObject instanceof String shape)
                    || shape.isBlank()) {
                continue;
            }

            List<RouteGeometryPoint> decoded =
                    decodePolyline6(shape);

            if (!points.isEmpty()
                    && !decoded.isEmpty()
                    && samePoint(
                            points.get(points.size() - 1),
                            decoded.get(0))) {

                decoded.remove(0);
            }

            points.addAll(decoded);
        }

        if (points.size() < 2) {
            throw new IllegalStateException(
                    "Valhalla no devolvió geometría suficiente"
            );
        }

        return new RouteGeometryResponse(
                routeId,
                distanceMeters,
                durationSeconds,
                points
        );
    }

    private double numberValue(Object value) {

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return 0.0;
    }

    private boolean samePoint(
            RouteGeometryPoint first,
            RouteGeometryPoint second) {

        return Math.abs(
                first.getLatitud() - second.getLatitud()
        ) < 0.000001
                &&
                Math.abs(
                        first.getLongitud()
                                - second.getLongitud()
                ) < 0.000001;
    }

    private List<RouteGeometryPoint> decodePolyline6(
            String encoded) {

        List<RouteGeometryPoint> points =
                new ArrayList<>();

        int index = 0;
        long latitude = 0;
        long longitude = 0;

        while (index < encoded.length()) {

            long result = 0;
            int shift = 0;
            int value;

            do {
                value = encoded.charAt(index++) - 63;

                result |=
                        (long) (value & 0x1f) << shift;

                shift += 5;

            } while (value >= 0x20);

            long latitudeChange =
                    ((result & 1) != 0)
                            ? ~(result >> 1)
                            : (result >> 1);

            latitude += latitudeChange;

            result = 0;
            shift = 0;

            do {
                value = encoded.charAt(index++) - 63;

                result |=
                        (long) (value & 0x1f) << shift;

                shift += 5;

            } while (value >= 0x20);

            long longitudeChange =
                    ((result & 1) != 0)
                            ? ~(result >> 1)
                            : (result >> 1);

            longitude += longitudeChange;

            points.add(
                    new RouteGeometryPoint(
                            latitude / 1_000_000.0,
                            longitude / 1_000_000.0
                    )
            );
        }

        return points;
    }
}
