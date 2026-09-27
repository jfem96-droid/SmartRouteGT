package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.PassengerAlert;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.PassengerAlertRepository;

@RestController
@RequestMapping("/api/passenger-alerts")
public class PassengerAlertController {

    private final PassengerAlertRepository passengerAlertRepository;
    private final AuditLogRepository auditLogRepository;

    public PassengerAlertController(
            PassengerAlertRepository passengerAlertRepository,
            AuditLogRepository auditLogRepository) {

        this.passengerAlertRepository = passengerAlertRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    public ResponseEntity<PassengerAlert> crearAlerta(
            @RequestBody PassengerAlert passengerAlert) {

        passengerAlert.setEstado(PassengerAlert.Estado.PENDING);
        passengerAlert.setFechaHora(LocalDateTime.now());

        PassengerAlert alertaGuardada = passengerAlertRepository.save(passengerAlert);

        return ResponseEntity.ok(alertaGuardada);
    }

    @GetMapping
    public List<PassengerAlert> listarAlertas() {
        return passengerAlertRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAlertaPorId(@PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(passengerAlert);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> revisarAlerta(@PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        if (passengerAlert.getEstado() != PassengerAlert.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("La alerta ya fue procesada");
        }

        passengerAlert.setEstado(PassengerAlert.Estado.REVIEWED);

        PassengerAlert alertaGuardada = passengerAlertRepository.save(passengerAlert);

        AuditLog auditLog = new AuditLog(
                "PASSENGER_ALERT_REVIEWED",
                "ADMIN",
                "Alerta de pasajero enviada a revisión",
                "PASSENGER_ALERT",
                passengerAlert.getId());

        auditLogRepository.save(auditLog);

        return ResponseEntity.ok(alertaGuardada);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolverAlerta(@PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        if (passengerAlert.getEstado() != PassengerAlert.Estado.REVIEWED) {
            return ResponseEntity.badRequest()
                    .body("La alerta debe estar en revisión antes de resolverla");
        }

        passengerAlert.setEstado(PassengerAlert.Estado.RESOLVED);

        PassengerAlert alertaGuardada = passengerAlertRepository.save(passengerAlert);

        AuditLog auditLog = new AuditLog(
                "PASSENGER_ALERT_RESOLVED",
                "ADMIN",
                "Alerta de pasajero resuelta",
                "PASSENGER_ALERT",
                passengerAlert.getId());

        auditLogRepository.save(auditLog);

        return ResponseEntity.ok(alertaGuardada);
    }
}