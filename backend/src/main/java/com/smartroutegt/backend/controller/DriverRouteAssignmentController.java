package com.smartroutegt.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.Route;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.RouteRepository;
import com.smartroutegt.backend.service.ResourceAuthorizationService;
import com.smartroutegt.backend.service.RouteItineraryService;
import com.smartroutegt.backend.repository.TripRepository;

@RestController
@RequestMapping("/api/assignments")
public class DriverRouteAssignmentController {

    private final DriverRouteAssignmentRepository driverRouteAssignmentRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ResourceAuthorizationService authorizationService;
    private final RouteItineraryService itineraryService;
    private final TripRepository tripRepository;

    public DriverRouteAssignmentController(
            DriverRouteAssignmentRepository driverRouteAssignmentRepository,
            DriverRepository driverRepository,
            BusRepository busRepository,
            RouteRepository routeRepository,
            ResourceAuthorizationService authorizationService,
            RouteItineraryService itineraryService,
            TripRepository tripRepository) {

        this.driverRouteAssignmentRepository = driverRouteAssignmentRepository;
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.authorizationService = authorizationService;
        this.itineraryService = itineraryService;
        this.tripRepository = tripRepository;
    }

    @GetMapping
    public List<DriverRouteAssignment> obtenerAsignaciones() {
        return driverRouteAssignmentRepository.findAll();
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> obtenerAsignacionesPorDriver(
            @PathVariable Long driverId) {

        if (!authorizationService.canAccessDriver(driverId)) {
            return ResponseEntity.status(403)
                    .body("No tiene permiso para consultar las asignaciones de este piloto");
        }

        return ResponseEntity.ok(
                driverRouteAssignmentRepository.findByDriverId(driverId));
    }

    @PostMapping
    public ResponseEntity<?> crearAsignacion(
            @RequestBody AssignmentRequest request) {

        Driver driver = driverRepository
                .findById(request.getDriverId())
                .orElse(null);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        if (!driver.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El driver está inactivo");
        }

        Bus bus = busRepository
                .findById(request.getBusId())
                .orElse(null);

        if (bus == null) {
            return ResponseEntity.notFound().build();
        }

        if (!bus.isActiva()) {
            return ResponseEntity.badRequest()
                    .body("El bus está inactivo");
        }

        Route route = routeRepository
                .findById(request.getRouteId())
                .orElse(null);

        if (route == null) {
            return ResponseEntity.notFound().build();
        }

        if (!route.isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La ruta está inactiva");
        }

        var tipo = request.getTipoRecorrido() == null ?
            DriverRouteAssignment.TipoRecorrido.SOLO_IDA : request.getTipoRecorrido();
        if (tipo != DriverRouteAssignment.TipoRecorrido.SOLO_IDA && !itineraryService.hasReturnStops(route.getId())) {
            return ResponseEntity.badRequest().body("Primero configure las paradas de vuelta de la ruta");
        }
        if (tripRepository.existsByDriverIdAndActivoTrue(driver.getId()) ||
            tripRepository.findByBusIdAndActivoTrue(bus.getId()).isPresent()) {
            return ResponseEntity.status(409).body("No se puede cambiar una asignación mientras el piloto o el bus tiene un viaje activo");
        }

        // Un bus no puede estar asignado activamente a otro piloto.
        List<DriverRouteAssignment> busAssignments =
                driverRouteAssignmentRepository.findByBusIdAndActivaTrue(bus.getId());

        boolean busOcupado = busAssignments.stream()
                .anyMatch(a -> !a.getDriver().getId().equals(driver.getId()));

        if (busOcupado) {
            return ResponseEntity.badRequest()
                    .body("El bus ya tiene una asignación activa con otro piloto");
        }

        // Al cambiar la asignación del piloto, conservar la anterior
        // como historial en lugar de eliminarla.
        List<DriverRouteAssignment> anteriores =
                driverRouteAssignmentRepository.findByDriverIdAndActivaTrue(driver.getId());

        for (DriverRouteAssignment anterior : anteriores) {
            anterior.setActiva(false);
        }

        if (!anteriores.isEmpty()) {
            driverRouteAssignmentRepository.saveAll(anteriores);
        }

        DriverRouteAssignment assignment = new DriverRouteAssignment(driver, bus, route);
        assignment.setTipoRecorrido(tipo);

        return ResponseEntity.ok(
                driverRouteAssignmentRepository.save(assignment));
    }


    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> desasignar(
            @PathVariable Long id) {

        DriverRouteAssignment assignment =
                driverRouteAssignmentRepository.findById(id).orElse(null);

        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }

        if (!assignment.isActiva()) {
            return ResponseEntity.badRequest()
                    .body("La asignación ya está inactiva");
        }

        if (tripRepository.findByDriverIdAndActivoTrue(assignment.getDriver().getId()).isPresent()) {
            return ResponseEntity.status(409).body("Finalice el viaje activo antes de desasignar");
        }
        assignment.setActiva(false);
        driverRouteAssignmentRepository.save(assignment);

        return ResponseEntity.ok("Piloto desasignado correctamente");
    }

}