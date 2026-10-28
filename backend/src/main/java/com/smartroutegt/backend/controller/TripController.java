package com.smartroutegt.backend.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.RouteStopRepository;
import com.smartroutegt.backend.repository.StopVisitRepository;
import com.smartroutegt.backend.repository.TripRepository;
import com.smartroutegt.backend.service.ResourceAuthorizationService;
import com.smartroutegt.backend.service.StopVisitService;
import com.smartroutegt.backend.service.RouteItineraryService;
import com.smartroutegt.backend.entity.DriverRouteAssignment.TipoRecorrido;
import com.smartroutegt.backend.entity.Trip.Sentido;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import com.smartroutegt.backend.entity.TripAdministrativeClosure;
import com.smartroutegt.backend.repository.TripAdministrativeClosureRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/trips")
public class TripController {

        private final TripRepository tripRepository;
        private final DriverRouteAssignmentRepository assignmentRepository;
        private final RouteStopRepository routeStopRepository;
        private final StopVisitRepository stopVisitRepository;
        private final BusLocationRepository locationRepository;
        private final StopVisitService stopVisitService;
        private final ResourceAuthorizationService authorizationService;
        private final RouteItineraryService itineraryService;
        private final TripAdministrativeClosureRepository closureRepository;

        public TripController(
                        TripRepository tripRepository,
                        DriverRouteAssignmentRepository assignmentRepository,
                        RouteStopRepository routeStopRepository,
                        StopVisitRepository stopVisitRepository,
                        BusLocationRepository locationRepository,
                        StopVisitService stopVisitService,
                        ResourceAuthorizationService authorizationService,
                        RouteItineraryService itineraryService, TripAdministrativeClosureRepository closureRepository) {

                this.tripRepository = tripRepository;
                this.assignmentRepository = assignmentRepository;
                this.routeStopRepository = routeStopRepository;
                this.stopVisitRepository = stopVisitRepository;
                this.locationRepository = locationRepository;
                this.stopVisitService = stopVisitService;
                this.authorizationService = authorizationService;
                this.itineraryService = itineraryService;
                this.closureRepository = closureRepository;
        }

        @Transactional
        @PostMapping("/start")
        public ResponseEntity<?> iniciarViaje(@RequestParam Long assignmentId) {

                DriverRouteAssignment assignment = assignmentRepository.findById(assignmentId).orElse(null);

                if (assignment == null) {
                        return ResponseEntity.badRequest()
                                        .body("La asignación no existe");
                }

                if (!authorizationService.canAccessDriver(
                                assignment.getDriver().getId())) {

                        return ResponseEntity.status(403)
                                        .body("No tiene permiso para iniciar un viaje con esta asignación");
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

                TipoRecorrido tipo = assignment.getTipoRecorrido();
                Sentido sentido = tipo == TipoRecorrido.SOLO_VUELTA ? Sentido.VUELTA : Sentido.IDA;
                if (itineraryService.stops(assignment.getRoute().getId(), sentido).size() < 2) {
                        return ResponseEntity.badRequest().body("El recorrido autorizado no tiene paradas configuradas");
                }
                if (tripRepository.findByBusIdAndActivoTrue(assignment.getBus().getId()).isPresent()) {
                        return ResponseEntity.status(409).body("El bus ya tiene un viaje activo");
                }
                Trip trip = new Trip(
                                assignment.getDriver(),
                                assignment.getBus(),
                                assignment.getRoute());

                trip.setTipoRecorrido(tipo);
                trip.setSentidoActual(sentido);
                trip.setFechaInicio(LocalDateTime.now());
                trip.setActivo(true);

                Trip viajeGuardado = tripRepository.save(trip);

                return ResponseEntity.ok(viajeGuardado);
        }

        @Transactional
        @PutMapping("/{id}/next-leg")
        public ResponseEntity<?> comenzarVuelta(@PathVariable Long id) {
                Trip ida = tripRepository.findById(id).orElse(null);
                if (ida == null) return ResponseEntity.notFound().build();
                if (!authorizationService.canAccessDriver(ida.getDriver().getId())) return ResponseEntity.status(403).build();
                if (!ida.isActivo() || ida.getTipoRecorrido() != TipoRecorrido.IDA_Y_VUELTA || ida.getSentidoActual() != Sentido.IDA) {
                        return ResponseEntity.status(409).body("Esta etapa de vuelta no fue asignada por el administrador");
                }
                if (!itineraryService.hasReturnStops(ida.getRoute().getId())) {
                        return ResponseEntity.status(409).body("El recorrido de vuelta no está configurado");
                }
                ida.setFechaFin(LocalDateTime.now()); ida.setActivo(false);
                tripRepository.saveAndFlush(ida);
                Trip vuelta = new Trip(ida.getDriver(), ida.getBus(), ida.getRoute());
                vuelta.setTipoRecorrido(TipoRecorrido.IDA_Y_VUELTA);
                vuelta.setSentidoActual(Sentido.VUELTA);
                return ResponseEntity.ok(tripRepository.save(vuelta));
        }

        @Transactional
        @PutMapping("/{id}/finish")
        public ResponseEntity<?> finalizarViaje(@PathVariable Long id) {

                Trip trip = tripRepository.findById(id).orElse(null);

                if (trip == null) {
                        return ResponseEntity.badRequest()
                                        .body("El viaje no existe");
                }
                if (!authorizationService.canAccessDriver(
                                trip.getDriver().getId())) {

                        return ResponseEntity.status(403)
                                        .body("No tiene permiso para finalizar este viaje");
                }

                if (!trip.isActivo()) {
                        return ResponseEntity.badRequest()
                                        .body("El viaje ya está finalizado");
                }

                if (trip.getTipoRecorrido() == TipoRecorrido.IDA_Y_VUELTA && trip.getSentidoActual() == Sentido.IDA) {
                        return ResponseEntity.status(409).body("El administrador asignó ida y vuelta; complete la vuelta autorizada");
                }
                trip.setFechaFin(LocalDateTime.now());
                trip.setActivo(false);

                Trip viajeFinalizado = tripRepository.save(trip);

                return ResponseEntity.ok(viajeFinalizado);
        }

        /** Cierre administrativo con motivo obligatorio y registro auditable. Nunca es una baja física. */
        public record AdminFinishRequest(String reason) {}
        @Transactional
        @PutMapping("/{id}/admin-finish")
        public ResponseEntity<?> adminFinish(@PathVariable Long id, @RequestBody AdminFinishRequest request) {
                if (request == null || request.reason() == null || request.reason().trim().length() < 15
                        || request.reason().length() > 1000) {
                        return ResponseEntity.badRequest().body("Indique un motivo de 15 a 1000 caracteres");
                }
                Trip trip = tripRepository.findById(id).orElse(null);
                if (trip == null) return ResponseEntity.notFound().build();
                if (!trip.isActivo()) return ResponseEntity.status(409).body("Este viaje ya está cerrado");
                BusLocation last = locationRepository.findFirstByTripIdOrderByFechaHoraDesc(id).orElse(null);
                if (last != null && last.getFechaHora() != null &&
                    last.getFechaHora().isAfter(LocalDateTime.now().minusMinutes(5)) && last.getVelocidad() > 5.0) {
                    return ResponseEntity.status(409).body("Viaje con GPS reciente en movimiento. No es seguro finalizarlo desde el centro de control.");
                }
                String actor = SecurityContextHolder.getContext().getAuthentication().getName();
                trip.setFechaFin(LocalDateTime.now());
                trip.setActivo(false);
                tripRepository.save(trip);
                closureRepository.save(new TripAdministrativeClosure(id, actor, request.reason().trim()));
                return ResponseEntity.ok(Map.of("tripId", id, "closed", true));
        }

        @GetMapping("/active")
        public List<Trip> obtenerViajesActivos() {
                return tripRepository.findByActivoTrue();
        }

        @GetMapping("/driver/{driverId}/active")
        public ResponseEntity<?> obtenerViajeActivoPorPiloto(
                        @PathVariable Long driverId) {

                if (!authorizationService.canAccessDriver(driverId)) {
                        return ResponseEntity.status(403)
                                        .body("No tiene permiso para consultar los viajes de este piloto");
                }

                return tripRepository.findByDriverIdAndActivoTrue(driverId)
                                .<ResponseEntity<?>>map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @GetMapping("/{tripId}/next-stop")
        public ResponseEntity<?> obtenerProximaParada(
                        @PathVariable Long tripId) {

                Trip trip = tripRepository.findById(tripId).orElse(null);

                if (trip == null) {
                        return ResponseEntity.notFound().build();
                }

                List<RouteStop> paradasRuta = routeStopRepository
                                .findByRouteIdOrderByOrdenAsc(trip.getRoute().getId());
                if (trip.getSentidoActual() == Sentido.VUELTA) {
                        int[] order = {0};
                        paradasRuta = itineraryService.stops(trip.getRoute().getId(), Sentido.VUELTA)
                                .stream().map(stop -> new RouteStop(trip.getRoute(), stop, ++order[0])).toList();
                }

                for (RouteStop routeStop : paradasRuta) {

                        boolean cumplida = stopVisitRepository
                                        .existsByTripIdAndStopIdAndCumplidaTrue(
                                                        tripId,
                                                        routeStop.getStop().getId());

                        if (!cumplida) {

                                Map<String, Object> respuesta = new LinkedHashMap<>();

                                respuesta.put("tripId", trip.getId());
                                respuesta.put("stopId", routeStop.getStop().getId());
                                respuesta.put("parada", routeStop.getStop().getNombre());
                                respuesta.put("orden", routeStop.getOrden());
                                respuesta.put("latitud", routeStop.getStop().getLatitud());
                                respuesta.put("longitud", routeStop.getStop().getLongitud());
                                respuesta.put(
                                                "radioPermitido",
                                                routeStop.getStop().getRadioPermitido());

                                BusLocation ultimaUbicacion = locationRepository
                                                .findFirstByTripIdOrderByFechaHoraDesc(tripId)
                                                .orElse(null);

                                if (ultimaUbicacion != null) {

                                        double distanciaMetros = stopVisitService
                                                        .calcularDistanciaHaversine(
                                                                        ultimaUbicacion.getLatitud(),
                                                                        ultimaUbicacion.getLongitud(),
                                                                        routeStop.getStop().getLatitud(),
                                                                        routeStop.getStop().getLongitud());

                                        respuesta.put(
                                                        "distanciaMetros",
                                                        Math.round(distanciaMetros * 100.0) / 100.0);
                                        double velocidadActualKmh = ultimaUbicacion.getVelocidad();

                                        final double VELOCIDAD_MINIMA_MOVIMIENTO_KMH = 5.0;
                                        final double VELOCIDAD_REFERENCIA_ETA_KMH = 15.0;

                                        boolean busEnMovimiento = velocidadActualKmh >= VELOCIDAD_MINIMA_MOVIMIENTO_KMH;

                                        double velocidadCalculoKmh = busEnMovimiento
                                                        ? velocidadActualKmh
                                                        : VELOCIDAD_REFERENCIA_ETA_KMH;

                                        double velocidadMetrosSegundo = velocidadCalculoKmh / 3.6;

                                        long etaSegundos = Math.round(
                                                        distanciaMetros / velocidadMetrosSegundo);

                                        long etaMinutos = (long) Math.ceil(
                                                        etaSegundos / 60.0);

                                        respuesta.put(
                                                        "velocidadActualKmh",
                                                        Math.round(velocidadActualKmh * 10.0) / 10.0);

                                        respuesta.put(
                                                        "velocidadCalculoKmh",
                                                        Math.round(velocidadCalculoKmh * 10.0) / 10.0);

                                        respuesta.put("etaSegundos", etaSegundos);
                                        respuesta.put("etaMinutos", etaMinutos);

                                        respuesta.put(
                                                        "estadoEta",
                                                        busEnMovimiento
                                                                        ? "EN_MOVIMIENTO"
                                                                        : "BUS_DETENIDO");

                                        respuesta.put(
                                                        "ultimaActualizacion",
                                                        ultimaUbicacion.getFechaHora());

                                } else {
                                        respuesta.put("distanciaMetros", null);
                                        respuesta.put("velocidadActualKmh", null);
                                        respuesta.put("velocidadCalculoKmh", null);
                                        respuesta.put("etaSegundos", null);
                                        respuesta.put("etaMinutos", null);
                                        respuesta.put("estadoEta", "SIN_GPS");
                                        respuesta.put("ultimaActualizacion", null);
                                }

                                return ResponseEntity.ok(respuesta);
                        }
                }

                return ResponseEntity.ok(
                                "No hay paradas pendientes");
        }
}