package com.smartroutegt.backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.BusLocationRepository;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;
import com.smartroutegt.backend.repository.TripRepository;

@RestController
@RequestMapping("/api/passenger")
public class PassengerBusController {

    private final BusRepository busRepository;
    private final DriverRouteAssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final BusLocationRepository locationRepository;

    public PassengerBusController(
            BusRepository busRepository,
            DriverRouteAssignmentRepository assignmentRepository,
            TripRepository tripRepository,
            BusLocationRepository locationRepository) {

        this.busRepository = busRepository;
        this.assignmentRepository = assignmentRepository;
        this.tripRepository = tripRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/buses")
    public ResponseEntity<List<Map<String, Object>>> getPassengerBuses() {

        List<Bus> buses = busRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Bus bus : buses) {

            if (!bus.isActiva()) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();

            item.put("busId", bus.getId());
            item.put("codigo", bus.getCodigo());
            item.put("placa", bus.getPlaca());
            item.put("capacidad", bus.getCapacidad());
            item.put("activo", bus.isActiva());

            List<DriverRouteAssignment> assignments =
                    assignmentRepository.findByBusIdAndActivaTrue(bus.getId());

            if (!assignments.isEmpty()) {

                DriverRouteAssignment assignment = assignments.get(0);

                item.put("routeId", assignment.getRoute().getId());
                item.put("ruta", assignment.getRoute().getNombre());

            } else {

                item.put("routeId", null);
                item.put("ruta", null);
            }

            Trip trip = tripRepository
                    .findByBusIdAndActivoTrue(bus.getId())
                    .orElse(null);

            if (trip != null) {

                item.put("tripId", trip.getId());
                item.put("viajeActivo", true);
                item.put("direction", trip.getSentidoActual().name());
                item.put("tipoRecorrido", trip.getTipoRecorrido().name());

                item.put("routeId", trip.getRoute().getId());
                item.put("ruta", trip.getRoute().getNombre());

            } else {

                item.put("tripId", null);
                item.put("viajeActivo", false);
                item.put("direction", null);
                item.put("tipoRecorrido", null);
            }

            BusLocation location = locationRepository
                    .findFirstByBusIdOrderByFechaHoraDesc(bus.getId())
                    .orElse(null);

            if (location != null) {

                item.put("latitud", location.getLatitud());
                item.put("longitud", location.getLongitud());
                item.put("velocidad", location.getVelocidad());
                item.put(
                        "ultimaActualizacion",
                        location.getFechaHora());

            } else {

                item.put("latitud", null);
                item.put("longitud", null);
                item.put("velocidad", null);
                item.put("ultimaActualizacion", null);
            }

            response.add(item);
        }

        return ResponseEntity.ok(response);
    }
}
