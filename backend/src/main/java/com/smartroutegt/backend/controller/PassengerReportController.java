package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.PassengerReport;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.PassengerReportRepository;
import com.smartroutegt.backend.repository.TripRepository;

@RestController
@RequestMapping("/api/passenger-reports")
public class PassengerReportController {

    private static final int MINUTOS_DESPUES_DEL_VIAJE = 5;

    private final PassengerReportRepository passengerReportRepository;
    private final TripRepository tripRepository;
    private final AuditLogRepository auditLogRepository;

    public PassengerReportController(
            PassengerReportRepository passengerReportRepository,
            TripRepository tripRepository,
            AuditLogRepository auditLogRepository) {

        this.passengerReportRepository = passengerReportRepository;
        this.tripRepository = tripRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearReporte(
            @RequestBody PassengerReportRequest request) {

        if (request.getTipo() == null) {
            return ResponseEntity.badRequest()
                    .body("El tipo de reporte es obligatorio");
        }

        if (request.getDescripcion() == null
                || request.getDescripcion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("La descripción es obligatoria");
        }

        if (request.getTripId() == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje es obligatorio");
        }

        if (request.getLatitud() == null
                || request.getLongitud() == null) {

            return ResponseEntity.badRequest()
                    .body("La ubicación es obligatoria para enviar un reporte");
        }

        if (request.getLatitud() < -90
                || request.getLatitud() > 90) {

            return ResponseEntity.badRequest()
                    .body("La latitud no es válida");
        }

        if (request.getLongitud() < -180
                || request.getLongitud() > 180) {

            return ResponseEntity.badRequest()
                    .body("La longitud no es válida");
        }

        Trip trip = tripRepository
                .findById(request.getTripId())
                .orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje indicado no existe");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (!trip.isActivo()) {

            if (trip.getFechaFin() == null) {
                return ResponseEntity.badRequest()
                        .body("El viaje finalizado no tiene una fecha de finalización válida");
            }

            LocalDateTime limiteReporte = trip.getFechaFin()
                    .plusMinutes(MINUTOS_DESPUES_DEL_VIAJE);

            if (ahora.isAfter(limiteReporte)) {
                return ResponseEntity.badRequest()
                        .body("El tiempo permitido para reportar este viaje ha finalizado");
            }
        }

        PassengerReport report = new PassengerReport();

        report.setTipo(request.getTipo());
        report.setDescripcion(request.getDescripcion().trim());
        report.setEstado(PassengerReport.Estado.PENDING);
        report.setFechaHora(ahora);
        report.setTrip(trip);
        report.setBus(trip.getBus());
        report.setDriver(trip.getDriver());
        report.setLatitud(request.getLatitud());
        report.setLongitud(request.getLongitud());

        PassengerReport reporteGuardado = passengerReportRepository.save(report);

        return ResponseEntity.ok(reporteGuardado);
    }

    @GetMapping
    public List<PassengerReport> listarReportes() {
        return passengerReportRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerReporte(
            @PathVariable Long id) {

        PassengerReport report = passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewReport(
            @PathVariable Long id) {

        PassengerReport report = passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("Solo los reportes pendientes pueden pasar a revisión");
        }

        report.setEstado(PassengerReport.Estado.UNDER_REVIEW);
        passengerReportRepository.save(report);

        AuditLog audit = new AuditLog();
        audit.setAccion("PASSENGER_REPORT_UNDER_REVIEW");
        audit.setActor(getAuthenticatedActor());
        audit.setDescripcion("Reporte de pasajero enviado a revisión");
        audit.setEntidad("PASSENGER_REPORT");
        audit.setEntidadId(report.getId());
        audit.setFechaHora(LocalDateTime.now());

        auditLogRepository.save(audit);

        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveReport(
            @PathVariable Long id,
            @RequestBody ResolutionRequest request) {

        PassengerReport report = passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.UNDER_REVIEW) {
            return ResponseEntity.badRequest()
                    .body("Solo los reportes en revisión pueden resolverse");
        }

        if (request.getResolucion() == null
                || request.getResolucion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("La resolución es obligatoria");
        }

        String resolucion = request.getResolucion().trim();

        report.setEstado(PassengerReport.Estado.RESOLVED);
        report.setResolucion(resolucion);

        passengerReportRepository.save(report);

        AuditLog audit = new AuditLog();
        audit.setAccion("PASSENGER_REPORT_RESOLVED");
        audit.setActor(getAuthenticatedActor());
        audit.setDescripcion("Reporte resuelto: " + resolucion);
        audit.setEntidad("PASSENGER_REPORT");
        audit.setEntidadId(report.getId());
        audit.setFechaHora(LocalDateTime.now());

        auditLogRepository.save(audit);

        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReport(
            @PathVariable Long id,
            @RequestBody ResolutionRequest request) {

        PassengerReport report = passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.UNDER_REVIEW) {
            return ResponseEntity.badRequest()
                    .body("Solo los reportes en revisión pueden rechazarse");
        }

        if (request.getResolucion() == null
                || request.getResolucion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("El motivo del rechazo es obligatorio");
        }

        String resolucion = request.getResolucion().trim();

        report.setEstado(PassengerReport.Estado.REJECTED);
        report.setResolucion(resolucion);

        passengerReportRepository.save(report);

        AuditLog audit = new AuditLog();
        audit.setAccion("PASSENGER_REPORT_REJECTED");
        audit.setActor(getAuthenticatedActor());
        audit.setDescripcion("Reporte rechazado: " + resolucion);
        audit.setEntidad("PASSENGER_REPORT");
        audit.setEntidadId(report.getId());
        audit.setFechaHora(LocalDateTime.now());

        auditLogRepository.save(audit);

        return ResponseEntity.ok(report);
    }

    public static class PassengerReportRequest {

        private PassengerReport.Tipo tipo;
        private String descripcion;
        private Long tripId;
        private Double latitud;
        private Double longitud;

        public PassengerReport.Tipo getTipo() {
            return tipo;
        }

        public void setTipo(PassengerReport.Tipo tipo) {
            this.tipo = tipo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

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
    }

    public static class ResolutionRequest {

        private String resolucion;

        public String getResolucion() {
            return resolucion;
        }

        public void setResolucion(String resolucion) {
            this.resolucion = resolucion;
        }
    }

    private String getAuthenticatedActor() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            return "SISTEMA";
        }

        return authentication.getName();
    }

}