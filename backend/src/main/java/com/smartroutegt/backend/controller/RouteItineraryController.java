package com.smartroutegt.backend.controller;

import java.util.*;
import com.smartroutegt.backend.entity.*;
import com.smartroutegt.backend.repository.*;
import com.smartroutegt.backend.service.RouteItineraryService;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/routes/{routeId}")
public class RouteItineraryController {
    private final RouteItineraryService itineraries;
    private final RouteRepository routes;
    private final StopRepository stops;
    private final RouteReturnStopRepository returnStops;
    private final TripRepository trips;
    public RouteItineraryController(RouteItineraryService itineraries, RouteRepository routes,
        StopRepository stops, RouteReturnStopRepository returnStops, TripRepository trips) {
        this.itineraries=itineraries; this.routes=routes; this.stops=stops;
        this.returnStops=returnStops; this.trips=trips;
    }
    public record ReturnStopsRequest(List<Long> stopIds) {}
    public record JourneyView(String direction, List<Long> stopIds, List<Stop> stops) {}
    @GetMapping("/itineraries")
    public List<JourneyView> getItineraries(@PathVariable Long routeId) {
        ensureRoute(routeId);
        List<JourneyView> result = new ArrayList<>();
        for (Trip.Sentido direction : Trip.Sentido.values()) {
            var list = itineraries.stops(routeId, direction);
            if (list.size() >= 2) result.add(new JourneyView(direction.name(), list.stream().map(Stop::getId).toList(), list));
        }
        return result;
    }
    @GetMapping("/journey-options")
    public List<JourneyView> journeyOptions(@PathVariable Long routeId,
        @RequestParam Long originStopId, @RequestParam Long destinationStopId) {
        ensureRoute(routeId);
        return itineraries.options(routeId, originStopId, destinationStopId).stream()
            .map(j -> new JourneyView(j.direction().name(), j.stops().stream().map(Stop::getId).toList(), j.stops()))
            .toList();
    }
    @Transactional
    @PutMapping("/return-stops")
    public List<RouteReturnStop> setReturnStops(@PathVariable Long routeId, @RequestBody ReturnStopsRequest request) {
        Route route = routes.findById(routeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada"));
        if (trips.findByActivoTrue().stream().anyMatch(t -> t.getRoute().getId().equals(routeId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puede editar paradas mientras hay viajes activos en esta ruta");
        }
        List<Long> ids = request == null ? null : request.stopIds();
        if (ids == null || ids.size() < 2 || ids.size() > 200 || ids.contains(null) || new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configura al menos dos paradas únicas para la vuelta");
        }
        List<Stop> list = new ArrayList<>();
        for (Long id : ids) {
            Stop stop = stops.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parada inexistente: " + id));
            if (!stop.isActiva()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay una parada inactiva");
            list.add(stop);
        }
        returnStops.deleteByRouteId(routeId);
        returnStops.flush();
        for (int i=0;i<list.size();i++) returnStops.save(new RouteReturnStop(route,list.get(i),i+1));
        return returnStops.findByRouteIdOrderByOrdenAsc(routeId);
    }
    private void ensureRoute(Long routeId) {
        if (!routes.existsById(routeId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ruta no encontrada");
    }
}
