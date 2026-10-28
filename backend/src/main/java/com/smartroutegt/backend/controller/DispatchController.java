package com.smartroutegt.backend.controller;

import java.util.*;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.service.ResourceAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {
    private final DriverRouteAssignmentRepository assignments;
    private final ResourceAuthorizationService authorization;
    private final TripRepository trips;
    public DispatchController(DriverRouteAssignmentRepository assignments, ResourceAuthorizationService authorization, TripRepository trips) {
        this.assignments=assignments; this.authorization=authorization; this.trips=trips;
    }
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> tripState(@PathVariable Long tripId) {
        var trip = trips.findById(tripId).orElse(null);
        if (trip == null) return ResponseEntity.notFound().build();
        if (!authorization.canAccessDriver(trip.getDriver().getId())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of("tripId", tripId, "direction", trip.getSentidoActual().name(),
            "tipoRecorrido",trip.getTipoRecorrido().name(),"active",trip.isActivo()));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> current(@PathVariable Long driverId) {
        if (!authorization.canAccessDriver(driverId)) return ResponseEntity.status(403).build();
        var current = assignments.findByDriverIdAndActivaTrue(driverId);
        if (current.isEmpty()) return ResponseEntity.notFound().build();
        var a = current.get(0);
        return ResponseEntity.ok(Map.of("assignmentId",a.getId(),"driverId",driverId,
             "bus",a.getBus().getCodigo(),"route",a.getRoute().getNombre(),"routeId",a.getRoute().getId(),
             "tipoRecorrido",a.getTipoRecorrido().name()));
    }
}
