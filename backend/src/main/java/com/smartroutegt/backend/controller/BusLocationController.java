package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.entity.Violation;

import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.repository.ViolationRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.service.StopVisitService;

@RestController
@RequestMapping("/api/locations")
public class BusLocationController {

private static final double LIMITE_VELOCIDAD = 60.0;
private static final long SEGUNDOS_EXCESO_PERMITIDOS = 10;

private final BusLocationRepository locationRepository;
private final TripRepository tripRepository;
private final ViolationRepository violationRepository;
private final RouteStopRepository routeStopRepository;
private final StopVisitService stopVisitService;

public BusLocationController(
        BusLocationRepository locationRepository,
        TripRepository tripRepository,
        ViolationRepository violationRepository,
        RouteStopRepository routeStopRepository,
        StopVisitService stopVisitService) {

this.locationRepository = locationRepository;
this.tripRepository = tripRepository;
this.violationRepository = violationRepository;
this.routeStopRepository = routeStopRepository;
this.stopVisitService = stopVisitService;
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

        BusLocation locationGuardada =
                locationRepository.save(location);

        detectarExcesoVelocidad(
                trip,
                locationGuardada
        );
        procesarParadas(
        trip,
        locationGuardada
        );

        return ResponseEntity.ok(locationGuardada);
        }
private void procesarParadas(
        Trip trip,
        BusLocation locationActual) {

List<RouteStop> paradasRuta =
        routeStopRepository
                .findByRouteIdOrderByOrdenAsc(
                        trip.getRoute().getId());

for (RouteStop routeStop : paradasRuta) {

        stopVisitService.procesarUbicacion(
                trip,
                routeStop.getStop(),
                locationActual.getLatitud(),
                locationActual.getLongitud(),
                locationActual.getFechaHora()
        );
}
}

        private void detectarExcesoVelocidad(
        Trip trip,
        BusLocation locationActual) {

        if (locationActual.getVelocidad() <= LIMITE_VELOCIDAD) {
        return;
        }

        LocalDateTime inicioExceso;

        Optional<BusLocation> ultimaUbicacionNormal =
                locationRepository
                        .findFirstByTripIdAndVelocidadLessThanEqualOrderByFechaHoraDesc(
                                trip.getId(),
                                LIMITE_VELOCIDAD);

        if (ultimaUbicacionNormal.isPresent()) {

        Optional<BusLocation> primeraUbicacionExceso =
                locationRepository
                        .findFirstByTripIdAndFechaHoraAfterOrderByFechaHoraAsc(
                                trip.getId(),
                                ultimaUbicacionNormal
                                        .get()
                                        .getFechaHora());

        if (primeraUbicacionExceso.isEmpty()) {
                return;
        }

        inicioExceso =
                primeraUbicacionExceso
                        .get()
                        .getFechaHora();

        } else {

        Optional<BusLocation> primeraUbicacionViaje =
                locationRepository
                        .findFirstByTripIdOrderByFechaHoraAsc(
                                trip.getId());

        if (primeraUbicacionViaje.isEmpty()) {
                return;
        }

        inicioExceso =
                primeraUbicacionViaje
                        .get()
                        .getFechaHora();
        }

        long segundosExceso =
                Duration.between(
                        inicioExceso,
                        locationActual.getFechaHora()
                ).getSeconds();

        if (segundosExceso < SEGUNDOS_EXCESO_PERMITIDOS) {
        return;
        }

        boolean yaExisteInfraccion =
                violationRepository
                        .existsByTripIdAndTipoAndFechaHoraAfter(
                                trip.getId(),
                                "SPEEDING",
                                inicioExceso);

        if (yaExisteInfraccion) {
        return;
        }

        Violation violation = new Violation(
                trip.getDriver(),
                trip.getBus(),
                trip,
                "SPEEDING",
                "Exceso de velocidad durante más de "
                        + SEGUNDOS_EXCESO_PERMITIDOS
                        + " segundos",
                locationActual.getVelocidad(),
                LIMITE_VELOCIDAD
        );

        violationRepository.save(violation);
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
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
}
}