package com.smartroutegt.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.StopVisitRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.service.ResourceAuthorizationService;

@RestController
@RequestMapping("/api/stop-visits")
public class StopVisitController {

    private final StopVisitRepository stopVisitRepository;
    private final TripRepository tripRepository;
    private final ResourceAuthorizationService authorizationService;

    public StopVisitController(
            StopVisitRepository stopVisitRepository,
            TripRepository tripRepository,
            ResourceAuthorizationService authorizationService) {

        this.stopVisitRepository = stopVisitRepository;
        this.tripRepository = tripRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> obtenerVisitasPorViaje(
            @PathVariable Long tripId) {

        Trip trip = tripRepository.findById(tripId).orElse(null);

        if (trip == null) {
            return ResponseEntity.notFound().build();
        }

        if (!authorizationService.canAccessDriver(
                trip.getDriver().getId())) {

            return ResponseEntity.status(403)
                    .body("No tiene permiso para consultar las paradas de este viaje");
        }

        return ResponseEntity.ok(
                stopVisitRepository.findByTripId(tripId));
    }
}