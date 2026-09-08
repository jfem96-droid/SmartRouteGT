package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.repository.BusRepository;

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
@RequestMapping("/api/buses")
public class BusController {

    private final BusRepository busRepository;

    public BusController(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    @GetMapping
    public List<Bus> obtenerBuses() {
        return busRepository.findAll();
    }

    @PostMapping
    public Bus crearBus(@RequestBody Bus bus) {
        return busRepository.save(bus);
    }
    @GetMapping("/{id}")
public ResponseEntity<?> obtenerBusPorId(@PathVariable Long id) {

    Bus bus = busRepository.findById(id).orElse(null);

    if (bus == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(bus);
}@PutMapping("/{id}")
public ResponseEntity<?> actualizarBus(
        @PathVariable Long id,
        @RequestBody Bus datosNuevos) {

    Bus bus = busRepository.findById(id).orElse(null);

    if (bus == null) {
        return ResponseEntity.notFound().build();
    }

    bus.setCodigo(datosNuevos.getCodigo());
    bus.setPlaca(datosNuevos.getPlaca());
    bus.setCapacidad(datosNuevos.getCapacidad());

    Bus busActualizado = busRepository.save(bus);

    return ResponseEntity.ok(busActualizado);
}
@DeleteMapping("/{id}")
public ResponseEntity<?> desactivarBus(@PathVariable Long id) {

    Bus bus = busRepository.findById(id).orElse(null);

    if (bus == null) {
        return ResponseEntity.notFound().build();
    }

    bus.setActiva(false);
    busRepository.save(bus);

    return ResponseEntity.ok("Bus desactivado correctamente");
}
@PutMapping("/{id}/activar")
public ResponseEntity<?> activarBus(@PathVariable Long id) {

    Bus bus = busRepository.findById(id).orElse(null);

    if (bus == null) {
        return ResponseEntity.notFound().build();
    }

    bus.setActiva(true);
    busRepository.save(bus);

    return ResponseEntity.ok("Bus activado correctamente");
}
}