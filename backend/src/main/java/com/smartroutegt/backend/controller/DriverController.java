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

import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.service.BubbleSortService;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository driverRepository;
    private final BubbleSortService bubbleSortService;

    public DriverController(
            DriverRepository driverRepository,
            BubbleSortService bubbleSortService) {

        this.driverRepository = driverRepository;
        this.bubbleSortService = bubbleSortService;
    }

    @GetMapping
    public List<Driver> obtenerPilotos() {
        return driverRepository.findAll();
    }

    @GetMapping("/ordenados-por-sanciones")
    public List<Driver> obtenerPilotosOrdenadosPorSanciones() {
        List<Driver> drivers = driverRepository.findAll();
        return bubbleSortService.ordenarPorSancionesConfirmadas(drivers);
    }

    @PostMapping
    public Driver crearPiloto(@RequestBody Driver driver) {
        return driverRepository.save(driver);
    }

    @GetMapping("/license/{licencia}")
    public ResponseEntity<?> obtenerPilotoPorLicencia(
            @PathVariable String licencia) {

        Driver driver = driverRepository.findByLicencia(licencia);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(driver);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPilotoPorId(@PathVariable Long id) {

        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPiloto(
            @PathVariable Long id,
            @RequestBody Driver datosNuevos) {

        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setNombre(datosNuevos.getNombre());
        driver.setLicencia(datosNuevos.getLicencia());
        driver.setTelefono(datosNuevos.getTelefono());

        Driver driverActualizado = driverRepository.save(driver);

        return ResponseEntity.ok(driverActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarPiloto(@PathVariable Long id) {

        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setActivo(false);
        driverRepository.save(driver);

        return ResponseEntity.ok("Piloto desactivado correctamente");
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarPiloto(@PathVariable Long id) {

        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        driver.setActivo(true);
        driverRepository.save(driver);

        return ResponseEntity.ok("Piloto activado correctamente");
    }
}