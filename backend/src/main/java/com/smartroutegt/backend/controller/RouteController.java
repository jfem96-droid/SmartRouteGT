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
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Route;
import com.smartroutegt.backend.repository.RouteRepository;

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
    public ResponseEntity<?> crearRuta(@RequestBody Route route) {

        if (route.getCodigo() == null ||
                route.getCodigo().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El código de la ruta es obligatorio");
        }

        if (route.getNombre() == null ||
                route.getNombre().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El nombre de la ruta es obligatorio");
        }

        if (routeRepository.existsByCodigo(route.getCodigo())) {

            return ResponseEntity.badRequest()
                    .body("Ya existe una ruta con ese código");
        }

        Route routeGuardada = routeRepository.save(route);

        return ResponseEntity.ok(routeGuardada);
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

        if (datosNuevos.getCodigo() == null ||
                datosNuevos.getCodigo().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El código de la ruta es obligatorio");
        }

        if (datosNuevos.getNombre() == null ||
                datosNuevos.getNombre().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El nombre de la ruta es obligatorio");
        }

        if (routeRepository.existsByCodigoAndIdNot(
                datosNuevos.getCodigo(), id)) {

            return ResponseEntity.badRequest()
                    .body("Ya existe otra ruta con ese código");
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