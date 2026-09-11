package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.algorithm.GraphService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
public class AlgorithmController {

    private final GraphService graphService;

    public AlgorithmController(
            GraphService graphService) {

        this.graphService = graphService;
    }

    @PostMapping("/connection")
    public String agregarConexion(
            @RequestParam Long origen,
            @RequestParam Long destino,
            @RequestParam double peso) {

        graphService.agregarConexion(
                origen,
                destino,
                peso
        );

        return "Conexión agregada correctamente";
    }

    @GetMapping("/dijkstra")
    public List<Long> calcularRuta(
            @RequestParam Long origen,
            @RequestParam Long destino) {

        return graphService.dijkstra(
                origen,
                destino
        );
    }

    @GetMapping("/route/dijkstra")
    public List<Long> calcularRutaReal(
            @RequestParam Long routeId,
            @RequestParam Long origen,
            @RequestParam Long destino) {

        graphService.construirGrafoDesdeRuta(
                routeId
        );

        return graphService.dijkstra(
                origen,
                destino
        );
    }
}