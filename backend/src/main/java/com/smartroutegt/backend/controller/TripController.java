package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.TripRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepository;
    private final DriverRouteAssignmentRepository assignmentRepository;

    public TripController(
            TripRepository tripRepository,
            DriverRouteAssignmentRepository assignmentRepository) {

        this.tripRepository = tripRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<?> iniciarViaje(@RequestParam Long assignmentId) {

        DriverRouteAssignment assignment =
                assignmentRepository.findById(assignmentId).orElse(null);

        if (assignment == null) {
            return ResponseEntity.badRequest()
                    .body("La asignación no existe");
        }

        if (!assignment.isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La asignación está inactiva");
        }

        if (!assignment.getDriver().isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El piloto está inactivo");
        }

        if (!assignment.getBus().isActiva()) {
            return ResponseEntity.badRequest()
                    .body("El bus está inactivo");
        }

        if (!assignment.getRoute().isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La ruta está inactiva");
        }
        if (tripRepository.existsByDriverIdAndActivoTrue(
        assignment.getDriver().getId())) {

    return ResponseEntity.badRequest()
            .body("El piloto ya tiene un viaje activo");
}

        Trip trip = new Trip(
                assignment.getDriver(),
                assignment.getBus(),
                assignment.getRoute()
        );

        trip.setFechaInicio(LocalDateTime.now());
        trip.setActivo(true);

        Trip viajeGuardado = tripRepository.save(trip);

        return ResponseEntity.ok(viajeGuardado);
    }
    @GetMapping("/driver/{driverId}/active")
public ResponseEntity<?> obtenerViajeActivoPorPiloto(
        @PathVariable Long driverId) {

    return tripRepository.findByDriverIdAndActivoTrue(driverId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() ->
                    ResponseEntity.notFound().build());
}
    @PutMapping("/{id}/finish")
public ResponseEntity<?> finalizarViaje(@PathVariable Long id) {

    Trip trip = tripRepository.findById(id).orElse(null);

    if (trip == null) {
        return ResponseEntity.badRequest()
                .body("El viaje no existe");
    }

    if (!trip.isActivo()) {
        return ResponseEntity.badRequest()
                .body("El viaje ya está finalizado");
    }

    trip.setFechaFin(LocalDateTime.now());
    trip.setActivo(false);

    Trip viajeFinalizado = tripRepository.save(trip);

    return ResponseEntity.ok(viajeFinalizado);
}
}