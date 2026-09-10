package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.TripRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class BusLocationController {

    private final BusLocationRepository locationRepository;
    private final TripRepository tripRepository;

    public BusLocationController(
            BusLocationRepository locationRepository,
            TripRepository tripRepository) {

        this.locationRepository = locationRepository;
        this.tripRepository = tripRepository;
    }

    @PostMapping
    public ResponseEntity<?> registrarUbicacion(
            @RequestParam Long tripId,
            @RequestParam double latitud,
            @RequestParam double longitud,
            @RequestParam double velocidad) {

        Trip trip = tripRepository.findById(tripId).orElse(null);

        if (trip == null) {
            return ResponseEntity.badRequest()
                    .body("El viaje no existe");
        }

        if (!trip.isActivo()) {
            return ResponseEntity.badRequest()
                    .body("El viaje ya está finalizado");
        }

        if (latitud < -90 || latitud > 90) {
            return ResponseEntity.badRequest()
                    .body("Latitud inválida");
        }

        if (longitud < -180 || longitud > 180) {
            return ResponseEntity.badRequest()
                    .body("Longitud inválida");
        }

        if (velocidad < 0) {
            return ResponseEntity.badRequest()
                    .body("La velocidad no puede ser negativa");
        }

        BusLocation location = new BusLocation(
                trip,
                trip.getBus(),
                latitud,
                longitud,
                velocidad
        );

        return ResponseEntity.ok(
                locationRepository.save(location)
        );
    }

    @GetMapping("/trip/{tripId}")
    public List<BusLocation> obtenerRecorrido(
            @PathVariable Long tripId) {

        return locationRepository
                .findByTripIdOrderByFechaHoraAsc(tripId);
    }

    @GetMapping("/bus/{busId}/latest")
    public ResponseEntity<?> obtenerUltimaUbicacion(
            @PathVariable Long busId) {

        return locationRepository
                .findFirstByBusIdOrderByFechaHoraDesc(busId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}