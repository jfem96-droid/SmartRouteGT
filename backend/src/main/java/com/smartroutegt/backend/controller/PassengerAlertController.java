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
import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.PassengerAlert;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.PassengerAlertRepository;
import com.smartroutegt.backend.repository.TripRepository;

@RestController
@RequestMapping("/api/passenger-alerts")
public class PassengerAlertController {

    private final PassengerAlertRepository passengerAlertRepository;
    private final AuditLogRepository auditLogRepository;
    private final TripRepository tripRepository;
    private final BusLocationRepository busLocationRepository;

    public PassengerAlertController(
            PassengerAlertRepository passengerAlertRepository,
            AuditLogRepository auditLogRepository,
            TripRepository tripRepository,
            BusLocationRepository busLocationRepository) {

        this.passengerAlertRepository = passengerAlertRepository;
        this.auditLogRepository = auditLogRepository;
        this.tripRepository = tripRepository;
        this.busLocationRepository = busLocationRepository;
    }

    @PostMapping
    public ResponseEntity<PassengerAlert> crearAlerta(
            @RequestBody PassengerAlert passengerAlert) {

        passengerAlert.setEstado(PassengerAlert.Estado.PENDING);
        passengerAlert.setFechaHora(LocalDateTime.now());

        PassengerAlert alertaGuardada = passengerAlertRepository.save(passengerAlert);

        return ResponseEntity.ok(alertaGuardada);
    }

    @PostMapping("/sos")
    public ResponseEntity<?> crearSos(
            @RequestBody SosRequest request) {

        if (request.getTripId() == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje es obligatorio para enviar un SOS");
        }

        if (request.getLatitud() == null
                || request.getLongitud() == null) {

            return ResponseEntity.badRequest()
                    .body("La ubicación del pasajero es obligatoria para enviar un SOS");
        }

        if (request.getLatitud() < -90
                || request.getLatitud() > 90) {

            return ResponseEntity.badRequest()
                    .body("La latitud del pasajero no es válida");
        }

        if (request.getLongitud() < -180
                || request.getLongitud() > 180) {

            return ResponseEntity.badRequest()
                    .body("La longitud del pasajero no es válida");
        }

        Trip trip = tripRepository.findById(request.getTripId())
                .orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje indicado no existe");
        }

        if (!trip.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El SOS solo puede enviarse durante un viaje activo");
        }

        BusLocation busLocation = busLocationRepository
                .findFirstByTripIdOrderByFechaHoraDesc(trip.getId())
                .orElse(null);

        if (busLocation == null) {
            return ResponseEntity.badRequest()
                    .body("No existe una ubicación GPS disponible para el bus");
        }

        PassengerAlert passengerAlert = new PassengerAlert();

        passengerAlert.setTipo(PassengerAlert.Tipo.SOS);
        passengerAlert.setDescripcion(
                request.getDescripcion() == null
                        || request.getDescripcion().trim().isEmpty()
                                ? "SOS de emergencia enviado por pasajero"
                                : request.getDescripcion().trim());

        passengerAlert.setLatitud(request.getLatitud());
        passengerAlert.setLongitud(request.getLongitud());
        passengerAlert.setBusLatitud(busLocation.getLatitud());
        passengerAlert.setBusLongitud(busLocation.getLongitud());
        passengerAlert.setTrip(trip);
        passengerAlert.setBus(trip.getBus());
        passengerAlert.setEstado(PassengerAlert.Estado.PENDING);
        passengerAlert.setFechaHora(LocalDateTime.now());

        PassengerAlert alertaGuardada = passengerAlertRepository.save(passengerAlert);

        AuditLog auditLog = new AuditLog(
                "PASSENGER_SOS_CREATED",
                "PASSENGER",
                "SOS de emergencia generado para el bus "
                        + trip.getBus().getCodigo(),
                "PASSENGER_ALERT",
                alertaGuardada.getId());

        auditLogRepository.save(auditLog);

        return ResponseEntity.ok(alertaGuardada);
    }

    @GetMapping
    public List<PassengerAlert> listarAlertas() {
        return passengerAlertRepository.findAll();
    }

    @GetMapping("/emergencies")
    public List<PassengerAlert> listarEmergenciasPendientes() {
        return passengerAlertRepository
                .findByTipoAndEstadoOrderByFechaHoraDesc(
                        PassengerAlert.Tipo.SOS,
                        PassengerAlert.Estado.PENDING);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerAlertaPorId(
            @PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(passengerAlert);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> revisarAlerta(
            @PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        if (passengerAlert.getEstado() != PassengerAlert.Estado.PENDING) {

            return ResponseEntity.badRequest()
                    .body("La alerta ya fue procesada");
        }

        passengerAlert.setEstado(
                PassengerAlert.Estado.REVIEWED);

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
    public ResponseEntity<?> resolverAlerta(
            @PathVariable Long id) {

        PassengerAlert passengerAlert = passengerAlertRepository.findById(id).orElse(null);

        if (passengerAlert == null) {
            return ResponseEntity.notFound().build();
        }

        if (passengerAlert.getEstado() != PassengerAlert.Estado.REVIEWED) {

            return ResponseEntity.badRequest()
                    .body("La alerta debe estar en revisión antes de resolverla");
        }

        passengerAlert.setEstado(
                PassengerAlert.Estado.RESOLVED);

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

    public static class SosRequest {

        private Long tripId;
        private Double latitud;
        private Double longitud;
        private String descripcion;

        public Long getTripId() {
            return tripId;
        }

        public void setTripId(Long tripId) {
            this.tripId = tripId;
        }

        public Double getLatitud() {
            return latitud;
        }

        public void setLatitud(Double latitud) {
            this.latitud = latitud;
        }

        public Double getLongitud() {
            return longitud;
        }

        public void setLongitud(Double longitud) {
            this.longitud = longitud;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }
}