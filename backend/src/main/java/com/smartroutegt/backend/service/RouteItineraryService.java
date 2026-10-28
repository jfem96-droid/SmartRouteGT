package com.smartroutegt.backend.service;

import java.util.*;
import com.smartroutegt.backend.entity.*;
import com.smartroutegt.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RouteItineraryService {
    private final RouteStopRepository idaRepository;
    private final RouteReturnStopRepository vueltaRepository;
    public RouteItineraryService(RouteStopRepository idaRepository, RouteReturnStopRepository vueltaRepository) {
        this.idaRepository = idaRepository; this.vueltaRepository = vueltaRepository;
    }
    public List<Stop> stops(Long routeId, Trip.Sentido direction) {
        if (direction == Trip.Sentido.VUELTA) {
            return vueltaRepository.findByRouteIdOrderByOrdenAsc(routeId).stream().map(RouteReturnStop::getStop).toList();
        }
        return idaRepository.findByRouteIdOrderByOrdenAsc(routeId).stream().map(RouteStop::getStop).toList();
    }
    public boolean hasReturnStops(Long routeId) { return stops(routeId, Trip.Sentido.VUELTA).size() >= 2; }
    public record Journey(Trip.Sentido direction, List<Stop> stops) {}
    public List<Journey> options(Long routeId, Long originId, Long destinationId) {
        List<Journey> options = new ArrayList<>();
        for (Trip.Sentido direction : Trip.Sentido.values()) {
            List<Stop> stops = stops(routeId, direction);
            List<Long> ids = stops.stream().map(Stop::getId).toList();
            List<Long> selectedIds = RouteStopOrder.portion(ids, originId, destinationId);
            if (!selectedIds.isEmpty()) {
                // La selección se realiza por índices: admite varias apariciones sin invertir el trayecto.
                for (int i = 0; i < ids.size(); i++) {
                    if (!ids.get(i).equals(originId)) continue;
                    for (int j = i + 1; j < ids.size(); j++) {
                        if (ids.get(j).equals(destinationId)) {
                            options.add(new Journey(direction, new ArrayList<>(stops.subList(i, j + 1))));
                            i = ids.size(); break;
                        }
                    }
                }
            }
        }
        return options;
    }
    public Journey resolve(Long routeId, Long originId, Long destinationId, Trip.Sentido requestedDirection) {
        if ((originId == null) != (destinationId == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origen y destino deben indicarse juntos");
        }
        if (originId == null) {
            Trip.Sentido direction = requestedDirection == null ? Trip.Sentido.IDA : requestedDirection;
            List<Stop> stops = stops(routeId, direction);
            if (stops.size() < 2) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "El sentido no tiene paradas configuradas");
            return new Journey(direction, stops);
        }
        return options(routeId, originId, destinationId).stream()
            .filter(j -> requestedDirection == null || j.direction() == requestedDirection)
            .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "No hay recorrido autorizado entre estas paradas y en ese sentido"));
    }
}
