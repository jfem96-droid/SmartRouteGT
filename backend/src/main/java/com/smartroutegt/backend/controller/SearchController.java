package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.Bus;
import com.smartroutegt.backend.repository.BusRepository;
import com.smartroutegt.backend.service.SearchService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final BusRepository busRepository;
    private final SearchService searchService;

    public SearchController(
            BusRepository busRepository,
            SearchService searchService) {

        this.busRepository = busRepository;
        this.searchService = searchService;
    }

    @GetMapping("/buses/linear/{codigo}")
    public ResponseEntity<?> buscarBusLineal(
            @PathVariable String codigo) {

        List<Bus> buses = busRepository.findAll();

        Bus bus = searchService.busquedaLineal(buses, codigo);

        if (bus == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bus);
    }

    @GetMapping("/buses/binary/{codigo}")
    public ResponseEntity<?> buscarBusBinario(
            @PathVariable String codigo) {

        List<Bus> buses = busRepository.findAll();

        Bus bus = searchService.busquedaBinaria(buses, codigo);

        if (bus == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bus);
    }
}