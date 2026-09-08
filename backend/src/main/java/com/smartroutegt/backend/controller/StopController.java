package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.repository.StopRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

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
    public Stop crearParada(@RequestBody Stop stop) {
        return stopRepository.save(stop);
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