package com.smartroutegt.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Route;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.service.RouteItineraryService;
import com.smartroutegt.backend.repository.RouteRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopRepository;
import com.smartroutegt.backend.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/route-stops")
public class RouteStopController {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final TripRepository trips;
    private final RouteItineraryService itineraries;

    public RouteStopController(
            RouteStopRepository routeStopRepository,
            RouteRepository routeRepository,
            StopRepository stopRepository,
            TripRepository trips, RouteItineraryService itineraries) {

        this.routeStopRepository = routeStopRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.trips = trips;
        this.itineraries = itineraries;
    }

    @GetMapping
    public List<RouteStop> obtenerRelaciones() {
        return routeStopRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> agregarParadaARuta(
            @RequestParam Long routeId,
            @RequestParam Long stopId,
            @RequestParam int orden) {

        Route route = routeRepository.findById(routeId).orElse(null);
        Stop stop = stopRepository.findById(stopId).orElse(null);

        if (route == null) {
            return ResponseEntity.badRequest()
                    .body("La ruta no existe");
        }

        if (stop == null) {
            return ResponseEntity.badRequest()
                    .body("La parada no existe");
        }

        assertNoActiveTrip(routeId);
        if (orden <= 0) {
            return ResponseEntity.badRequest()
                    .body("El orden debe ser mayor que cero");
        }

        if (routeStopRepository.existsByRouteIdAndStopId(routeId, stopId)) {
            return ResponseEntity.status(409).body("Esta parada ya pertenece a la ruta");
        }

        if (routeStopRepository.existsByRouteIdAndOrden(routeId, orden)) {
            return ResponseEntity.status(409).body("Ya existe una parada con ese número de orden en la ruta");
        }

        RouteStop routeStop = new RouteStop(
                route,
                stop,
                orden);

        return ResponseEntity.ok(
                routeStopRepository.save(routeStop));
    }

    @GetMapping("/route")
    public List<RouteStop> obtenerParadasPorRuta(
            @RequestParam Long routeId,
            @RequestParam(required = false) Trip.Sentido direction) {
        if (direction == Trip.Sentido.VUELTA) {
            Route route = routeRepository.findById(routeId).orElseThrow();
            int[] orden = {0};
            return itineraries.stops(routeId, Trip.Sentido.VUELTA).stream()
                .map(stop -> new RouteStop(route, stop, ++orden[0])).toList();
        }
        return routeStopRepository
                .findByRouteIdOrderByOrdenAsc(routeId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> quitarParadaDeRuta(@PathVariable Long id) {
        RouteStop routeStop = routeStopRepository.findById(id).orElse(null);
        if (routeStop == null) return ResponseEntity.notFound().build();
        assertNoActiveTrip(routeStop.getRoute().getId());
        routeStopRepository.delete(routeStop);
        return ResponseEntity.ok("Parada quitada de la ruta");
    }

    @PutMapping("/{id}/configuracion")
    public ResponseEntity<?> actualizarConfiguracion(
            @PathVariable Long id,
            @RequestBody ConfiguracionParadaRequest request) {

        RouteStop routeStop = routeStopRepository.findById(id).orElse(null);

        if (routeStop == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getTipoParada() == null) {
            return ResponseEntity.badRequest()
                    .body("El tipo de parada es obligatorio");
        }

        if (request.getTiempoMinimoSegundos() == null
                || request.getTiempoObjetivoSegundos() == null
                || request.getTiempoMaximoSegundos() == null) {

            return ResponseEntity.badRequest()
                    .body("Todos los tiempos son obligatorios");
        }

        int minimo = request.getTiempoMinimoSegundos();
        int objetivo = request.getTiempoObjetivoSegundos();
        int maximo = request.getTiempoMaximoSegundos();

        if (minimo < 1) {
            return ResponseEntity.badRequest()
                    .body("El tiempo mínimo debe ser mayor que cero");
        }

        if (objetivo < minimo) {
            return ResponseEntity.badRequest()
                    .body("El tiempo objetivo no puede ser menor que el mínimo");
        }

        if (maximo < objetivo) {
            return ResponseEntity.badRequest()
                    .body("El tiempo máximo no puede ser menor que el objetivo");
        }

        if (maximo > 300) {
            return ResponseEntity.badRequest()
                    .body("El tiempo máximo no puede superar 300 segundos");
        }

        if (request.getTipoParada() == RouteStop.TipoParada.NORMAL
                && maximo > 60) {

            return ResponseEntity.badRequest()
                    .body("Una parada normal no puede superar 60 segundos");
        }

        if (request.getTipoParada() == RouteStop.TipoParada.ALTA_DEMANDA
                && maximo > 120) {

            return ResponseEntity.badRequest()
                    .body("Una parada de alta demanda no puede superar 120 segundos");
        }

        assertNoActiveTrip(routeStop.getRoute().getId());
        routeStop.setTipoParada(request.getTipoParada());
        routeStop.setTiempoMinimoSegundos(minimo);
        routeStop.setTiempoObjetivoSegundos(objetivo);
        routeStop.setTiempoMaximoSegundos(maximo);

        return ResponseEntity.ok(
                routeStopRepository.save(routeStop));
    }

    private void assertNoActiveTrip(Long routeId) {
        if (trips.findByActivoTrue().stream().anyMatch(t -> t.getRoute().getId().equals(routeId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La ruta tiene viajes activos; espere antes de modificar sus paradas");
        }
    }

    public static class ConfiguracionParadaRequest {

        private RouteStop.TipoParada tipoParada;
        private Integer tiempoMinimoSegundos;
        private Integer tiempoObjetivoSegundos;
        private Integer tiempoMaximoSegundos;

        public RouteStop.TipoParada getTipoParada() {
            return tipoParada;
        }

        public void setTipoParada(
                RouteStop.TipoParada tipoParada) {

            this.tipoParada = tipoParada;
        }

        public Integer getTiempoMinimoSegundos() {
            return tiempoMinimoSegundos;
        }

        public void setTiempoMinimoSegundos(
                Integer tiempoMinimoSegundos) {

            this.tiempoMinimoSegundos = tiempoMinimoSegundos;
        }

        public Integer getTiempoObjetivoSegundos() {
            return tiempoObjetivoSegundos;
        }

        public void setTiempoObjetivoSegundos(
                Integer tiempoObjetivoSegundos) {

            this.tiempoObjetivoSegundos = tiempoObjetivoSegundos;
        }

        public Integer getTiempoMaximoSegundos() {
            return tiempoMaximoSegundos;
        }

        public void setTiempoMaximoSegundos(
                Integer tiempoMaximoSegundos) {

            this.tiempoMaximoSegundos = tiempoMaximoSegundos;
        }
    }
}