package com.smartroutegt.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.entity.Violation;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.repository.ViolationRepository;

@RestController
@RequestMapping("/api/violations")
public class ViolationController {

    private final ViolationRepository violationRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final AuditLogRepository auditLogRepository;

    public ViolationController(
            ViolationRepository violationRepository,
            DriverRepository driverRepository,
            BusRepository busRepository,
            TripRepository tripRepository,
            AuditLogRepository auditLogRepository) {

        this.violationRepository = violationRepository;
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearInfraccion(
            @RequestBody ViolationRequest request) {

        String error = validarRequest(request);

        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        Driver driver = driverRepository
                .findById(request.getDriverId())
                .orElse(null);

        if (driver == null) {
            return ResponseEntity.badRequest()
                    .body("El piloto no existe");
        }

        Bus bus = busRepository
                .findById(request.getBusId())
                .orElse(null);

        if (bus == null) {
            return ResponseEntity.badRequest()
                    .body("El bus no existe");
        }

        Trip trip = tripRepository
                .findById(request.getTripId())
                .orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje no existe");
        }

        String coherencia = validarRelacion(driver, bus, trip);

        if (coherencia != null) {
            return ResponseEntity.badRequest().body(coherencia);
        }

        Violation violation = new Violation(
                driver,
                bus,
                trip,
                request.getTipo().trim(),
                request.getDescripcion().trim(),
                request.getVelocidadRegistrada(),
                request.getLimitePermitido());

        Violation guardada = violationRepository.save(violation);

        registrarAuditoria(
                "CREAR_INFRACCION",
                "Se creó manualmente una infracción pendiente",
                guardada.getId());

        return ResponseEntity.ok(guardada);
    }

    @GetMapping
    public List<Violation> obtenerInfracciones() {
        return violationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerInfraccionPorId(
            @PathVariable Long id) {

        Violation violation = violationRepository
                .findById(id)
                .orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(violation);
    }

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<?> modificarInfraccion(
            @PathVariable Long id,
            @RequestBody ViolationUpdateRequest request) {

        Violation violation = violationRepository
                .findById(id)
                .orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        if (violation.getEstado() != Violation.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("Solo se pueden modificar infracciones pendientes");
        }

        if (request.getTipo() == null
                || request.getTipo().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("El tipo es obligatorio");
        }

        if (request.getDescripcion() == null
                || request.getDescripcion().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("La descripción es obligatoria");
        }

        if (request.getVelocidadRegistrada() != null
                && request.getVelocidadRegistrada() < 0) {
            return ResponseEntity.badRequest()
                    .body("La velocidad registrada no puede ser negativa");
        }

        if (request.getLimitePermitido() != null
                && request.getLimitePermitido() <= 0) {
            return ResponseEntity.badRequest()
                    .body("El límite permitido debe ser mayor que cero");
        }

        if (request.getVelocidadRegistrada() != null
                && request.getLimitePermitido() != null
                && request.getVelocidadRegistrada() <= request.getLimitePermitido()) {
            return ResponseEntity.badRequest()
                    .body("La velocidad registrada debe superar el límite permitido");
        }

        violation.setTipo(request.getTipo().trim());
        violation.setDescripcion(request.getDescripcion().trim());
        violation.setVelocidadRegistrada(
                request.getVelocidadRegistrada());
        violation.setLimitePermitido(
                request.getLimitePermitido());

        Violation guardada = violationRepository.save(violation);

        registrarAuditoria(
                "MODIFICAR_INFRACCION",
                "Se modificó una infracción pendiente",
                guardada.getId());

        return ResponseEntity.ok(guardada);
    }

    @Transactional
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmarInfraccion(
            @PathVariable Long id) {

        Violation violation = violationRepository
                .findById(id)
                .orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        if (violation.getEstado() != Violation.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("Solo se pueden confirmar infracciones pendientes");
        }

        Driver driver = violation.getDriver();

        violation.setEstado(Violation.Estado.CONFIRMED);

        int nuevasSanciones = driver.getSancionesConfirmadas() + 1;

        driver.setSancionesConfirmadas(nuevasSanciones);

        if (nuevasSanciones >= 10) {
            driver.setActivo(false);
        }

        driverRepository.save(driver);

        Violation guardada = violationRepository.save(violation);

        registrarAuditoria(
                "CONFIRMAR_INFRACCION",
                "Se confirmó la infracción. Total de sanciones confirmadas: "
                        + nuevasSanciones,
                guardada.getId());

        return ResponseEntity.ok(guardada);
    }

    @Transactional
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rechazarInfraccion(
            @PathVariable Long id) {

        Violation violation = violationRepository
                .findById(id)
                .orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        if (violation.getEstado() != Violation.Estado.PENDING) {
            return ResponseEntity.badRequest()
                    .body("Solo se pueden rechazar infracciones pendientes");
        }

        violation.setEstado(Violation.Estado.REJECTED);

        Violation guardada = violationRepository.save(violation);

        registrarAuditoria(
                "RECHAZAR_INFRACCION",
                "Se rechazó la infracción",
                guardada.getId());

        return ResponseEntity.ok(guardada);
    }

    @Transactional
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelarInfraccion(
            @PathVariable Long id,
            @RequestBody CancelViolationRequest request) {

        Violation violation = violationRepository
                .findById(id)
                .orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        if (violation.getEstado() == Violation.Estado.CANCELLED) {
            return ResponseEntity.badRequest()
                    .body("La infracción ya está cancelada");
        }

        if (request.getJustificacion() == null
                || request.getJustificacion().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("La justificación es obligatoria");
        }

        boolean estabaConfirmada = violation.getEstado() == Violation.Estado.CONFIRMED;

        Driver driver = violation.getDriver();

        if (estabaConfirmada) {
            int sancionesActuales = driver.getSancionesConfirmadas();

            driver.setSancionesConfirmadas(
                    Math.max(0, sancionesActuales - 1));

            driverRepository.save(driver);
        }

        violation.setEstado(Violation.Estado.CANCELLED);

        Violation guardada = violationRepository.save(violation);

        String descripcion = "Se canceló la infracción. Justificación: "
                + request.getJustificacion().trim();

        if (estabaConfirmada) {
            descripcion += ". El contador del piloto se redujo a "
                    + driver.getSancionesConfirmadas()
                    + ". La reactivación del piloto no es automática";
        }

        registrarAuditoria(
                "CANCELAR_INFRACCION",
                descripcion,
                guardada.getId());

        return ResponseEntity.ok(guardada);
    }

    private String validarRequest(ViolationRequest request) {

        if (request.getDriverId() == null) {
            return "El piloto es obligatorio";
        }

        if (request.getBusId() == null) {
            return "El bus es obligatorio";
        }

        if (request.getTripId() == null) {
            return "El viaje es obligatorio";
        }

        if (request.getTipo() == null
                || request.getTipo().isBlank()) {
            return "El tipo es obligatorio";
        }

        if (request.getDescripcion() == null
                || request.getDescripcion().isBlank()) {
            return "La descripción es obligatoria";
        }

        if (request.getVelocidadRegistrada() != null
                && request.getVelocidadRegistrada() < 0) {
            return "La velocidad registrada no puede ser negativa";
        }

        if (request.getLimitePermitido() != null
                && request.getLimitePermitido() <= 0) {
            return "El límite permitido debe ser mayor que cero";
        }

        if (request.getVelocidadRegistrada() != null
                && request.getLimitePermitido() != null
                && request.getVelocidadRegistrada() <= request.getLimitePermitido()) {
            return "La velocidad registrada debe superar el límite permitido";
        }

        return null;
    }

    private String validarRelacion(
            Driver driver,
            Bus bus,
            Trip trip) {

        if (trip.getDriver() == null
                || trip.getDriver().getId() == null
                || !trip.getDriver().getId()
                        .equals(driver.getId())) {

            return "El viaje no pertenece al piloto indicado";
        }

        if (trip.getBus() == null
                || trip.getBus().getId() == null
                || !trip.getBus().getId()
                        .equals(bus.getId())) {

            return "El viaje no pertenece al bus indicado";
        }

        return null;
    }

    private void registrarAuditoria(
            String accion,
            String descripcion,
            Long entidadId) {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        String actor = "SISTEMA";

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null) {

            actor = authentication.getName();
        }

        AuditLog auditLog = new AuditLog(
                accion,
                actor,
                descripcion,
                "VIOLATION",
                entidadId);

        auditLogRepository.save(auditLog);
    }

    public static class ViolationRequest {

        private Long driverId;
        private Long busId;
        private Long tripId;
        private String tipo;
        private String descripcion;
        private Double velocidadRegistrada;
        private Double limitePermitido;

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

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Double getVelocidadRegistrada() {
            return velocidadRegistrada;
        }

        public void setVelocidadRegistrada(
                Double velocidadRegistrada) {
            this.velocidadRegistrada = velocidadRegistrada;
        }

        public Double getLimitePermitido() {
            return limitePermitido;
        }

        public void setLimitePermitido(
                Double limitePermitido) {
            this.limitePermitido = limitePermitido;
        }
    }

    public static class ViolationUpdateRequest {

        private String tipo;
        private String descripcion;
        private Double velocidadRegistrada;
        private Double limitePermitido;

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public Double getVelocidadRegistrada() {
            return velocidadRegistrada;
        }

        public void setVelocidadRegistrada(
                Double velocidadRegistrada) {
            this.velocidadRegistrada = velocidadRegistrada;
        }

        public Double getLimitePermitido() {
            return limitePermitido;
        }

        public void setLimitePermitido(
                Double limitePermitido) {
            this.limitePermitido = limitePermitido;
        }
    }

    public static class CancelViolationRequest {

        private String justificacion;

        public String getJustificacion() {
            return justificacion;
        }

        public void setJustificacion(String justificacion) {
            this.justificacion = justificacion;
        }
    }
}