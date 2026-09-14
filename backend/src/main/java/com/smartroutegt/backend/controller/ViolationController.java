package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.entity.Violation;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.repository.ViolationRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/violations")
public class ViolationController {

    private final ViolationRepository violationRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;

    public ViolationController(
            ViolationRepository violationRepository,
            DriverRepository driverRepository,
            BusRepository busRepository,
            TripRepository tripRepository) {

        this.violationRepository = violationRepository;
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
        this.tripRepository = tripRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearInfraccion(
            @RequestBody ViolationRequest request) {

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

        Violation violation = new Violation(
                driver,
                bus,
                trip,
                request.getTipo(),
                request.getDescripcion(),
                request.getVelocidadRegistrada(),
                request.getLimitePermitido()
        );

        Violation infraccionGuardada =
                violationRepository.save(violation);

        return ResponseEntity.ok(infraccionGuardada);
    }

    @GetMapping
    public List<Violation> obtenerInfracciones() {
        return violationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerInfraccionPorId(
            @PathVariable Long id) {

        Violation violation =
                violationRepository.findById(id).orElse(null);

        if (violation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(violation);
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
}