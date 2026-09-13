package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.PassengerAlert;
import com.smartroutegt.backend.repository.PassengerAlertRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.repository.AuditLogRepository;

import java.util.List;
import java.time.LocalDateTime;

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
    @GetMapping("/status/{estado}")
public ResponseEntity<?> listarPorEstado(
        @PathVariable PassengerAlert.Estado estado) {

    List<PassengerAlert> alertas =
            passengerAlertRepository.findByEstado(estado);

    return ResponseEntity.ok(alertas);
}

@PutMapping("/{id}/review")
public ResponseEntity<?> marcarComoRevisada(@PathVariable Long id) {

    PassengerAlert alerta =
            passengerAlertRepository.findById(id).orElse(null);

    if (alerta == null) {
        return ResponseEntity.notFound().build();
    }

    if (alerta.getEstado() != PassengerAlert.Estado.PENDING) {
        return ResponseEntity.badRequest()
                .body("La alerta no está pendiente");
    }

    alerta.setEstado(PassengerAlert.Estado.REVIEWED);

    PassengerAlert guardada =
            passengerAlertRepository.save(alerta);

    auditLogRepository.save(new AuditLog(
            "PASSENGER_ALERT_REVIEWED",
            "PASSENGER_ALERT",
            alerta.getId(),
            "ADMIN",
            "Alerta de pasajero marcada como revisada"));

    return ResponseEntity.ok(guardada);
}

@PutMapping("/{id}/resolve")
public ResponseEntity<?> resolverAlerta(@PathVariable Long id) {

    PassengerAlert alerta =
            passengerAlertRepository.findById(id).orElse(null);

    if (alerta == null) {
        return ResponseEntity.notFound().build();
    }

    if (alerta.getEstado() != PassengerAlert.Estado.REVIEWED) {
        return ResponseEntity.badRequest()
                .body("La alerta debe estar revisada antes de resolverse");
    }

    alerta.setEstado(PassengerAlert.Estado.RESOLVED);

    PassengerAlert guardada =
            passengerAlertRepository.save(alerta);

    auditLogRepository.save(new AuditLog(
            "PASSENGER_ALERT_RESOLVED",
            "PASSENGER_ALERT",
            alerta.getId(),
            "ADMIN",
            "Alerta de pasajero resuelta"));

    return ResponseEntity.ok(guardada);
}
}
