package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.Route;

import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.RouteRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/assignments")
public class DriverRouteAssignmentController {

    private final DriverRouteAssignmentRepository driverRouteAssignmentRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;

    public DriverRouteAssignmentController(
            DriverRouteAssignmentRepository driverRouteAssignmentRepository,
            DriverRepository driverRepository,
            BusRepository busRepository,
            RouteRepository routeRepository) {

        this.driverRouteAssignmentRepository = driverRouteAssignmentRepository;
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
    }

    @GetMapping
    public List<DriverRouteAssignment> obtenerAsignaciones() {
        return driverRouteAssignmentRepository.findAll();
    }

    @GetMapping("/driver/{driverId}")
    public List<DriverRouteAssignment> obtenerAsignacionesPorDriver(@PathVariable Long driverId) {
        return driverRouteAssignmentRepository.findByDriverId(driverId);
    }

    @PostMapping
    public ResponseEntity<?> crearAsignacion(@RequestBody AssignmentRequest request) {

        Driver driver = driverRepository.findById(request.getDriverId()).orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        if (!driver.isActivo()) {
            return ResponseEntity.badRequest().body("El driver está inactivo");
        }

        Bus bus = busRepository.findById(request.getBusId()).orElse(null);

        if (bus == null) {
            return ResponseEntity.notFound().build();
        }

        if (!bus.isActiva()) {
            return ResponseEntity.badRequest().body("El bus está inactivo");
        }

        Route route = routeRepository.findById(request.getRouteId()).orElse(null);

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        if (!route.isActiva()) {
            return ResponseEntity.badRequest().body("La ruta está inactiva");
        }
        if (driverRouteAssignmentRepository
        .existsByDriverIdAndActivaTrue(driver.getId())) {

        return ResponseEntity.badRequest()
            .body("El piloto ya tiene una asignación activa");
        }

        if (driverRouteAssignmentRepository
        .existsByBusIdAndActivaTrue(bus.getId())) {

        return ResponseEntity.badRequest()
            .body("El bus ya tiene una asignación activa");
        }

        DriverRouteAssignment assignment =
                new DriverRouteAssignment(driver, bus, route);

        return ResponseEntity.ok(
                driverRouteAssignmentRepository.save(assignment)
                
        );
        
    }
    @PutMapping("/{id}/deactivate")
public ResponseEntity<?> desactivarAsignacion(
        @PathVariable Long id) {

    DriverRouteAssignment assignment =
            driverRouteAssignmentRepository
                    .findById(id)
                    .orElse(null);

    if (assignment == null) {
        return ResponseEntity.notFound().build();
    }

    if (!assignment.isActiva()) {
        return ResponseEntity.badRequest()
                .body("La asignación ya se encuentra inactiva");
    }

    assignment.setActiva(false);

    driverRouteAssignmentRepository.save(assignment);

    return ResponseEntity.ok(
            "Asignación desactivada correctamente");
    }
}
