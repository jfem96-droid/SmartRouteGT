package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Route;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.repository.RouteRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopRepository;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return ResponseEntity.badRequest().body("La ruta no existe");
        }

        if (stop == null) {
            return ResponseEntity.badRequest().body("La parada no existe");
        }

        RouteStop routeStop = new RouteStop(route, stop, orden);

        return ResponseEntity.ok(routeStopRepository.save(routeStop));
    }
    @GetMapping("/route")
public List<RouteStop> obtenerParadasPorRuta(@RequestParam Long routeId) {
    return routeStopRepository.findByRouteIdOrderByOrdenAsc(routeId);
}
}