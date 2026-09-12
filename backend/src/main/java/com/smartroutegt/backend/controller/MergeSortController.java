package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.service.MergeSortService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merge-sort")
public class MergeSortController {

    private final MergeSortService mergeSortService;

    public MergeSortController(
            MergeSortService mergeSortService) {

        this.mergeSortService = mergeSortService;
    }

    @GetMapping("/buses/capacidad")
    public List<Bus> obtenerBusesPorCapacidad() {
        return mergeSortService
                .obtenerBusesOrdenadosPorCapacidad();
    }
}