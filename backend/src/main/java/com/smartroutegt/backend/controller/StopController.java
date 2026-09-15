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

import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.repository.StopRepository;

@RestController
@RequestMapping("/api/stops")
public class StopController {

    private final StopRepository stopRepository;

    public StopController(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    @GetMapping
    public List<Stop> obtenerParadas() {
        return stopRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crearParada(@RequestBody Stop stop) {

        if (stop.getNombre() == null ||
                stop.getNombre().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El nombre de la parada es obligatorio");
        }

        if (stop.getLatitud() < -90 ||
                stop.getLatitud() > 90) {

            return ResponseEntity.badRequest()
                    .body("La latitud debe estar entre -90 y 90");
        }

        if (stop.getLongitud() < -180 ||
                stop.getLongitud() > 180) {

            return ResponseEntity.badRequest()
                    .body("La longitud debe estar entre -180 y 180");
        }

        if (stop.getRadioPermitido() <= 0) {

            return ResponseEntity.badRequest()
                    .body("El radio permitido debe ser mayor que 0");
        }

        Stop stopGuardada = stopRepository.save(stop);

        return ResponseEntity.ok(stopGuardada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerParadaPorId(@PathVariable Long id) {

        Stop stop = stopRepository.findById(id).orElse(null);

        if (stop == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stop);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarParada(
            @PathVariable Long id,
            @RequestBody Stop datosNuevos) {

        Stop stop = stopRepository.findById(id).orElse(null);

        if (stop == null) {
            return ResponseEntity.notFound().build();
        }

        if (datosNuevos.getNombre() == null ||
                datosNuevos.getNombre().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("El nombre de la parada es obligatorio");
        }

        if (datosNuevos.getLatitud() < -90 ||
                datosNuevos.getLatitud() > 90) {

            return ResponseEntity.badRequest()
                    .body("La latitud debe estar entre -90 y 90");
        }

        if (datosNuevos.getLongitud() < -180 ||
                datosNuevos.getLongitud() > 180) {

            return ResponseEntity.badRequest()
                    .body("La longitud debe estar entre -180 y 180");
        }

        if (datosNuevos.getRadioPermitido() <= 0) {

            return ResponseEntity.badRequest()
                    .body("El radio permitido debe ser mayor que 0");
        }

        stop.setNombre(datosNuevos.getNombre());
        stop.setLatitud(datosNuevos.getLatitud());
        stop.setLongitud(datosNuevos.getLongitud());
        stop.setRadioPermitido(datosNuevos.getRadioPermitido());

        Stop stopActualizada = stopRepository.save(stop);

        return ResponseEntity.ok(stopActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarParada(@PathVariable Long id) {

        Stop stop = stopRepository.findById(id).orElse(null);

        if (stop == null) {
            return ResponseEntity.notFound().build();
        }

        stop.setActiva(false);
        stopRepository.save(stop);

        return ResponseEntity.ok("Parada desactivada correctamente");
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarParada(@PathVariable Long id) {

        Stop stop = stopRepository.findById(id).orElse(null);

        if (stop == null) {
            return ResponseEntity.notFound().build();
        }

        stop.setActiva(true);
        stopRepository.save(stop);

        return ResponseEntity.ok("Parada activada correctamente");
    }
}