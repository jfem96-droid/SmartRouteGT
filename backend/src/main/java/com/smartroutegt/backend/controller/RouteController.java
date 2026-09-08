package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Route;
import com.smartroutegt.backend.repository.RouteRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteRepository routeRepository;

    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GetMapping
    public List<Route> obtenerRutas() {
        return routeRepository.findAll();
    }

    @PostMapping
    public Route crearRuta(@RequestBody Route route) {
        return routeRepository.save(route);
    }
    @GetMapping("/{id}")
public ResponseEntity<?> obtenerRutaPorId(@PathVariable Long id) {

    Route route = routeRepository.findById(id).orElse(null);

    if (route == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(route);
}
@PutMapping("/{id}")
public ResponseEntity<?> actualizarRuta(
        @PathVariable Long id,
        @RequestBody Route datosNuevos) {

    Route route = routeRepository.findById(id).orElse(null);

    if (route == null) {
        return ResponseEntity.notFound().build();
    }

    route.setCodigo(datosNuevos.getCodigo());
    route.setNombre(datosNuevos.getNombre());
    route.setActiva(datosNuevos.isActiva());

    Route routeActualizada = routeRepository.save(route);

    return ResponseEntity.ok(routeActualizada);
}
@DeleteMapping("/{id}")
public ResponseEntity<?> desactivarRuta(@PathVariable Long id) {

    Route route = routeRepository.findById(id).orElse(null);

    if (route == null) {
        return ResponseEntity.notFound().build();
    }

    route.setActiva(false);
    routeRepository.save(route);

    return ResponseEntity.ok("Ruta desactivada correctamente");
}
@PutMapping("/{id}/activar")
public ResponseEntity<?> activarRuta(@PathVariable Long id) {

    Route route = routeRepository.findById(id).orElse(null);

    if (route == null) {
        return ResponseEntity.notFound().build();
    }

    route.setActiva(true);
    routeRepository.save(route);

    return ResponseEntity.ok("Ruta activada correctamente");
}
}