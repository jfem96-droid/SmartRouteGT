package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.algorithm.NearestBusService;
import com.smartroutegt.backend.entity.BusLocation;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/algorithms")
public class NearestBusController {

    private final NearestBusService nearestBusService;

    public NearestBusController(
            NearestBusService nearestBusService) {

        this.nearestBusService = nearestBusService;
    }

    @GetMapping("/nearest-bus")
    public BusLocation obtenerBusMasCercano(
            @RequestParam double latitud,
            @RequestParam double longitud) {

        return nearestBusService.encontrarBusMasCercano(
                latitud,
                longitud
        );
    }
}