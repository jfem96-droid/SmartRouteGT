package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.PassengerAlert;
import com.smartroutegt.backend.repository.PassengerAlertRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passenger-alerts")
public class PassengerAlertController {

    private final PassengerAlertRepository passengerAlertRepository;

    public PassengerAlertController(PassengerAlertRepository passengerAlertRepository) {
        this.passengerAlertRepository = passengerAlertRepository;
    }

    @PostMapping
    public ResponseEntity<PassengerAlert> crearAlerta(
            @RequestBody PassengerAlert passengerAlert) {

        PassengerAlert alertaGuardada =
                passengerAlertRepository.save(passengerAlert);

        return ResponseEntity.ok(alertaGuardada);
    }

    @GetMapping
    public List<PassengerAlert> listarAlertas() {
        return passengerAlertRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAlertaPorId(@PathVariable Long id) {

        PassengerAlert passengerAlert =
                passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(passengerAlert);
    }
}
