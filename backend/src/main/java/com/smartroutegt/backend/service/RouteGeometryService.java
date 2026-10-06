package com.smartroutegt.backend.service;

import com.smartroutegt.backend.dto.routing.RouteGeometryPoint;
import com.smartroutegt.backend.dto.routing.RouteGeometryResponse;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.repository.RouteStopRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteGeometryService {

    private static final String VALHALLA_URL =
            "https://valhalla1.openstreetmap.de/route";

    private final RouteStopRepository routeStopRepository;
    private final RestClient restClient;

    public RouteGeometryService(
            RouteStopRepository routeStopRepository) {

        this.routeStopRepository = routeStopRepository;

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

        List<RouteStop> routeStops =
                routeStopRepository
                        .findByRouteIdOrderByOrdenAsc(routeId);

        if (originStopId != null || destinationStopId != null) {

            if (originStopId == null || destinationStopId == null) {
                throw new IllegalArgumentException(
                        "Debes enviar originStopId y destinationStopId"
                );
            }

            int originIndex = -1;
            int destinationIndex = -1;

            for (int i = 0; i < routeStops.size(); i++) {

                Long stopId = routeStops.get(i).getStop().getId();

                if (stopId.equals(originStopId)) {
                    originIndex = i;
                }

                if (stopId.equals(destinationStopId)) {
                    destinationIndex = i;
                }
            }

            if (originIndex < 0 || destinationIndex < 0) {
                throw new IllegalArgumentException(
                        "El origen o destino no pertenece a la ruta"
                );
            }

            if (originIndex >= destinationIndex) {
                throw new IllegalArgumentException(
                        "El destino debe estar después del origen en la ruta"
                );
            }

            routeStops = new ArrayList<>(
                    routeStops.subList(
                            originIndex,
                            destinationIndex + 1
                    )
            );
        }

        if (routeStops.size() < 2) {
            throw new IllegalArgumentException(
                    "La ruta necesita al menos dos paradas"
            );
        }

        List<Map<String, Object>> locations = new ArrayList<>();

        for (RouteStop routeStop : routeStops) {

            Map<String, Object> location =
                    new LinkedHashMap<>();

            location.put(
                    "lat",
                    routeStop.getStop().getLatitud()
            );

            location.put(
                    "lon",
                    routeStop.getStop().getLongitud()
            );

            location.put("type", "break");

            locations.add(location);
        }

        Map<String, Object> request =
                new LinkedHashMap<>();

        request.put("locations", locations);
        request.put("costing", "auto");
        request.put("units", "kilometers");

        Map<?, ?> response = restClient
                .post()
                .uri(VALHALLA_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

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
