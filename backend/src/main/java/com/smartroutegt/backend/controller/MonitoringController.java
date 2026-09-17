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
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final BusRepository busRepository;
    private final DriverRouteAssignmentRepository assignmentRepository;
    private final TripRepository tripRepository;
    private final BusLocationRepository locationRepository;

    public MonitoringController(
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
    public ResponseEntity<List<Map<String, Object>>> obtenerMonitoreoBuses() {

        List<Bus> buses = busRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Bus bus : buses) {

            Map<String, Object> informacion = new LinkedHashMap<>();

            informacion.put("busId", bus.getId());
            informacion.put("codigo", bus.getCodigo());
            informacion.put("placa", bus.getPlaca());
            informacion.put("capacidad", bus.getCapacidad());
            informacion.put("activo", bus.isActiva());

            List<DriverRouteAssignment> asignaciones = assignmentRepository.findByBusIdAndActivaTrue(bus.getId());

            if (!asignaciones.isEmpty()) {

                DriverRouteAssignment asignacion = asignaciones.get(0);

                informacion.put("driverId", asignacion.getDriver().getId());
                informacion.put("piloto", asignacion.getDriver().getNombre());
                informacion.put("routeId", asignacion.getRoute().getId());
                informacion.put("ruta", asignacion.getRoute().getNombre());

            } else {

                informacion.put("driverId", null);
                informacion.put("piloto", null);
                informacion.put("routeId", null);
                informacion.put("ruta", null);
            }

            Trip viajeActivo = tripRepository
                    .findByBusIdAndActivoTrue(bus.getId())
                    .orElse(null);

            if (viajeActivo != null) {
                informacion.put("tripId", viajeActivo.getId());
                informacion.put("viajeActivo", true);
            } else {
                informacion.put("tripId", null);
                informacion.put("viajeActivo", false);
            }

            BusLocation ultimaUbicacion = locationRepository
                    .findFirstByBusIdOrderByFechaHoraDesc(bus.getId())
                    .orElse(null);

            if (ultimaUbicacion != null) {

                informacion.put("latitud", ultimaUbicacion.getLatitud());
                informacion.put("longitud", ultimaUbicacion.getLongitud());
                informacion.put("velocidad", ultimaUbicacion.getVelocidad());
                informacion.put(
                        "ultimaActualizacion",
                        ultimaUbicacion.getFechaHora());

            } else {

                informacion.put("latitud", null);
                informacion.put("longitud", null);
                informacion.put("velocidad", null);
                informacion.put("ultimaActualizacion", null);
            }

            resultado.add(informacion);
        }

        return ResponseEntity.ok(resultado);
    }
}
