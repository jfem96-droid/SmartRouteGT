package com.smartroutegt.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import com.smartroutegt.backend.repository.RouteRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopRepository;

@RestController
@RequestMapping("/api/route-stops")
public class RouteStopController {

    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;

    public RouteStopController(
            RouteStopRepository routeStopRepository,
            RouteRepository routeRepository,
            StopRepository stopRepository) {

        this.routeStopRepository = routeStopRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
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

        if (orden <= 0) {
            return ResponseEntity.badRequest()
                    .body("El orden debe ser mayor que cero");
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
            @RequestParam Long routeId) {

        return routeStopRepository
                .findByRouteIdOrderByOrdenAsc(routeId);
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

        routeStop.setTipoParada(request.getTipoParada());
        routeStop.setTiempoMinimoSegundos(minimo);
        routeStop.setTiempoObjetivoSegundos(objetivo);
        routeStop.setTiempoMaximoSegundos(maximo);

        return ResponseEntity.ok(
                routeStopRepository.save(routeStop));
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