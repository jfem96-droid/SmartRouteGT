package com.smartroutegt.backend.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.repository.RouteStopRepository;

@Service
public class BfsDfsService {

    private final RouteStopRepository routeStopRepository;

    public BfsDfsService(RouteStopRepository routeStopRepository) {
        this.routeStopRepository = routeStopRepository;
    }

    // BFS recorre la red de paradas por niveles.
    // Visita primero las paradas vecinas antes de continuar con las siguientes.
    public List<Long> bfs(Long startStopId) {

        Map<Long, Set<Long>> graph = construirGrafo();

        List<Long> recorrido = new ArrayList<>();

        if (!graph.containsKey(startStopId)) {
            return recorrido;
        }

        Queue<Long> cola = new ArrayDeque<>();
        Set<Long> visitados = new HashSet<>();

        cola.add(startStopId);
        visitados.add(startStopId);

        while (!cola.isEmpty()) {

            Long actual = cola.poll();
            recorrido.add(actual);

            for (Long vecino : graph.get(actual)) {

                if (visitados.add(vecino)) {
                    cola.add(vecino);
                }
            }
        }

        return recorrido;
    }

    // DFS recorre la red de paradas en profundidad.
    // Avanza por una conexión hasta donde sea posible antes de retroceder.
    public List<Long> dfs(Long startStopId) {

        Map<Long, Set<Long>> graph = construirGrafo();

        List<Long> recorrido = new ArrayList<>();

        if (!graph.containsKey(startStopId)) {
            return recorrido;
        }

        Set<Long> visitados = new HashSet<>();

        dfsRecursivo(
                startStopId,
                graph,
                visitados,
                recorrido);

        return recorrido;
    }

    private void dfsRecursivo(
            Long actual,
            Map<Long, Set<Long>> graph,
            Set<Long> visitados,
            List<Long> recorrido) {

        visitados.add(actual);
        recorrido.add(actual);

        for (Long vecino : graph.get(actual)) {

            if (!visitados.contains(vecino)) {
                dfsRecursivo(
                        vecino,
                        graph,
                        visitados,
                        recorrido);
            }
        }
    }

    private Map<Long, Set<Long>> construirGrafo() {

        List<RouteStop> routeStops = new ArrayList<>(routeStopRepository.findAll());

        routeStops.sort(
                (a, b) -> {
                    int comparacionRuta = Long.compare(
                            a.getRoute().getId(),
                            b.getRoute().getId());

                    if (comparacionRuta != 0) {
                        return comparacionRuta;
                    }

                    return Integer.compare(
                            a.getOrden(),
                            b.getOrden());
                });

        Map<Long, List<RouteStop>> paradasPorRuta = new LinkedHashMap<>();

        for (RouteStop routeStop : routeStops) {

            Long routeId = routeStop.getRoute().getId();

            paradasPorRuta
                    .computeIfAbsent(
                            routeId,
                            id -> new ArrayList<>())
                    .add(routeStop);
        }

        Map<Long, Set<Long>> graph = new LinkedHashMap<>();

        for (List<RouteStop> paradas : paradasPorRuta.values()) {

            for (RouteStop routeStop : paradas) {

                Long stopId = routeStop.getStop().getId();

                graph.computeIfAbsent(
                        stopId,
                        id -> new LinkedHashSet<>());
            }

            for (int i = 0; i < paradas.size() - 1; i++) {

                Long origen = paradas.get(i).getStop().getId();

                Long destino = paradas.get(i + 1).getStop().getId();

                graph.get(origen).add(destino);
                graph.get(destino).add(origen);
            }
        }

        return graph;
    }
}