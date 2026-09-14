package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.PassengerReport;
import com.smartroutegt.backend.entity.Trip;

import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.PassengerReportRepository;
import com.smartroutegt.backend.repository.TripRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/passenger-reports")
public class PassengerReportController {

    private final PassengerReportRepository passengerReportRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final AuditLogRepository auditLogRepository;

    public PassengerReportController(
            PassengerReportRepository passengerReportRepository,
            DriverRepository driverRepository,
            BusRepository busRepository,
            TripRepository tripRepository,
            AuditLogRepository auditLogRepository) {

        this.passengerReportRepository = passengerReportRepository;
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearReporte(
            @RequestBody PassengerReportRequest request) {

        Driver driver =
                driverRepository.findById(request.getDriverId()).orElse(null);

        if (driver == null) {
            return ResponseEntity.badRequest()
                    .body("El piloto no existe");
        }

        Bus bus =
                busRepository.findById(request.getBusId()).orElse(null);

        if (bus == null) {
            return ResponseEntity.badRequest()
                    .body("El bus no existe");
        }

        Trip trip =
                tripRepository.findById(request.getTripId()).orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje no existe");
        }

        PassengerReport report = new PassengerReport();

        report.setDriver(driver);
        report.setBus(bus);
        report.setTrip(trip);
        report.setTipo(request.getTipo());
        report.setDescripcion(request.getDescripcion());
        report.setEstado(PassengerReport.Estado.PENDING);
        report.setFechaHora(LocalDateTime.now());

        return ResponseEntity.ok(
                passengerReportRepository.save(report));
    }

    @GetMapping
    public List<PassengerReport> listarReportes() {
        return passengerReportRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerReporte(@PathVariable Long id) {

        return passengerReportRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{estado}")
    public List<PassengerReport> listarPorEstado(
            @PathVariable PassengerReport.Estado estado) {

        return passengerReportRepository.findByEstado(estado);
    }

    @GetMapping("/driver/{driverId}")
    public List<PassengerReport> listarPorPiloto(
            @PathVariable Long driverId) {

        return passengerReportRepository.findByDriverId(driverId);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> ponerEnRevision(@PathVariable Long id) {

        PassengerReport report =
                passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("El reporte no está pendiente");
        }

        report.setEstado(PassengerReport.Estado.UNDER_REVIEW);

        PassengerReport guardado =
                passengerReportRepository.save(report);

        auditLogRepository.save(new AuditLog(
                "PASSENGER_REPORT_UNDER_REVIEW",
                "PASSENGER_REPORT",
                report.getId(),
                "ADMIN",
                "Reporte de pasajero enviado a revisión"));

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolverReporte(
            @PathVariable Long id,
            @RequestBody ResolutionRequest request) {

        PassengerReport report =
                passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.UNDER_REVIEW) {
            return ResponseEntity.badRequest()
                    .body("El reporte debe estar en revisión");
        }

        if (request.getResolucion() == null ||
                request.getResolucion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("La resolución es obligatoria");
        }

        report.setEstado(PassengerReport.Estado.RESOLVED);
        report.setResolucion(request.getResolucion());

        PassengerReport guardado =
                passengerReportRepository.save(report);

        auditLogRepository.save(new AuditLog(
                "PASSENGER_REPORT_RESOLVED",
                "PASSENGER_REPORT",
                report.getId(),
                "ADMIN",
                "Reporte resuelto: " + request.getResolucion()));

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rechazarReporte(
            @PathVariable Long id,
            @RequestBody ResolutionRequest request) {

        PassengerReport report =
                passengerReportRepository.findById(id).orElse(null);

        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        if (report.getEstado() != PassengerReport.Estado.UNDER_REVIEW) {
            return ResponseEntity.badRequest()
                    .body("El reporte debe estar en revisión");
        }

        if (request.getResolucion() == null ||
                request.getResolucion().trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("La razón del rechazo es obligatoria");
        }

        report.setEstado(PassengerReport.Estado.REJECTED);
        report.setResolucion(request.getResolucion());

        PassengerReport guardado =
                passengerReportRepository.save(report);

        auditLogRepository.save(new AuditLog(
                "PASSENGER_REPORT_REJECTED",
                "PASSENGER_REPORT",
                report.getId(),
                "ADMIN",
                "Reporte rechazado: " + request.getResolucion()));

        return ResponseEntity.ok(guardado);
    }

    public static class PassengerReportRequest {

        private Long driverId;
        private Long busId;
        private Long tripId;
        private PassengerReport.Tipo tipo;
        private String descripcion;

        public Long getDriverId() {
            return driverId;
        }

        public void setDriverId(Long driverId) {
            this.driverId = driverId;
        }

        public Long getBusId() {
            return busId;
        }

        public void setBusId(Long busId) {
            this.busId = busId;
        }

        public Long getTripId() {
            return tripId;
        }

        public void setTripId(Long tripId) {
            this.tripId = tripId;
        }

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
}