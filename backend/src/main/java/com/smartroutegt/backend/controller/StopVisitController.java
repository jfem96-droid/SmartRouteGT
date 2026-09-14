package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.StopVisit;
import com.smartroutegt.backend.repository.StopVisitRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stop-visits")
public class StopVisitController {

    private final StopVisitRepository stopVisitRepository;

    public StopVisitController(
            StopVisitRepository stopVisitRepository) {

        this.stopVisitRepository = stopVisitRepository;
    }

    @GetMapping("/trip/{tripId}")
    public List<StopVisit> obtenerVisitasPorViaje(
            @PathVariable Long tripId) {

        return stopVisitRepository.findByTripId(tripId);
    }
}