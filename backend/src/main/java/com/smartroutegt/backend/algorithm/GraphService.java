package com.smartroutegt.backend.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.repository.RouteStopRepository;

@Service
public class GraphService {

        private final Map<Long, List<Edge>> graph = new HashMap<>();

        private final RouteStopRepository routeStopRepository;

        public GraphService(RouteStopRepository routeStopRepository) {
                this.routeStopRepository = routeStopRepository;
        }

        public void agregarConexion(
                        Long origen,
                        Long destino,
                        double peso) {

                graph.putIfAbsent(origen, new ArrayList<>());
                graph.putIfAbsent(destino, new ArrayList<>());

                graph.get(origen).add(
                                new Edge(destino, peso));

                graph.get(destino).add(
                                new Edge(origen, peso));
        }

        public Map<Long, List<Edge>> obtenerGrafo() {
                return graph;
        }

        public List<Long> dijkstra(
                        Long origen,
                        Long destino) {

                Map<Long, Double> distancias = new HashMap<>();
                Map<Long, Long> anteriores = new HashMap<>();

                PriorityQueue<NodeDistance> cola = new PriorityQueue<>(
                                (nodo1, nodo2) -> Double.compare(
                                                nodo1.getDistancia(),
                                                nodo2.getDistancia()));

                for (Long nodo : graph.keySet()) {
                        distancias.put(
                                        nodo,
                                        Double.POSITIVE_INFINITY);
                }

                if (!graph.containsKey(origen)
                                || !graph.containsKey(destino)) {

                        return Collections.emptyList();
                }

                distancias.put(origen, 0.0);

                cola.add(
                                new NodeDistance(
                                                origen,
                                                0.0));

                while (!cola.isEmpty()) {

                        NodeDistance actual = cola.poll();

                        Long nodoActual = actual.getNodo();

                        if (actual.getDistancia() > distancias.get(nodoActual)) {

                                continue;
                        }

                        if (nodoActual.equals(destino)) {
                                break;
                        }

                        for (Edge edge : graph.get(nodoActual)) {

                                double nuevaDistancia = distancias.get(nodoActual)
                                                + edge.getPeso();

                                if (nuevaDistancia < distancias.get(
                                                edge.getDestino())) {

                                        distancias.put(
                                                        edge.getDestino(),
                                                        nuevaDistancia);

                                        anteriores.put(
                                                        edge.getDestino(),
                                                        nodoActual);

                                        cola.add(
                                                        new NodeDistance(
                                                                        edge.getDestino(),
                                                                        nuevaDistancia));
                                }
                        }
                }

                if (!origen.equals(destino)
                                && !anteriores.containsKey(destino)) {

                        return Collections.emptyList();
                }

                List<Long> ruta = new ArrayList<>();

                Long actual = destino;

                while (actual != null) {

                        ruta.add(actual);

                        if (actual.equals(origen)) {
                                break;
                        }

                        actual = anteriores.get(actual);
                }

                Collections.reverse(ruta);

                return ruta;
        }

        public double calcularDistancia(
                        double latitud1,
                        double longitud1,
                        double latitud2,
                        double longitud2) {

                final double RADIO_TIERRA_KM = 6371.0;

                double lat1Rad = Math.toRadians(latitud1);

                double lat2Rad = Math.toRadians(latitud2);

                double diferenciaLatitud = Math.toRadians(
                                latitud2 - latitud1);

                double diferenciaLongitud = Math.toRadians(
                                longitud2 - longitud1);

                double a = Math.sin(
                                diferenciaLatitud / 2)
                                * Math.sin(
                                                diferenciaLatitud / 2)
                                +
                                Math.cos(lat1Rad)
                                                * Math.cos(lat2Rad)
                                                * Math.sin(
                                                                diferenciaLongitud / 2)
                                                * Math.sin(
                                                                diferenciaLongitud / 2);

                double c = 2 * Math.atan2(
                                Math.sqrt(a),
                                Math.sqrt(1 - a));

                return RADIO_TIERRA_KM * c;
        }

        public void construirGrafoDesdeRuta(
                        Long routeId) {

                graph.clear();

                List<RouteStop> paradas = routeStopRepository
                                .findByRouteIdOrderByOrdenAsc(
                                                routeId);

                if (paradas.size() < 2) {
                        return;
                }

                for (int i = 0; i < paradas.size() - 1; i++) {

                        Stop origen = paradas.get(i)
                                        .getStop();

                        Stop destino = paradas.get(i + 1)
                                        .getStop();

                        double distancia = calcularDistancia(
                                        origen.getLatitud(),
                                        origen.getLongitud(),
                                        destino.getLatitud(),
                                        destino.getLongitud());

                        agregarConexion(
                                        origen.getId(),
                                        destino.getId(),
                                        distancia);
                }
        }

        public static class Edge {

                private final Long destino;
                private final double peso;

                public Edge(
                                Long destino,
                                double peso) {

                        this.destino = destino;
                        this.peso = peso;
                }

                public Long getDestino() {
                        return destino;
                }

                public double getPeso() {
                        return peso;
                }
        }

        private static class NodeDistance {

                private final Long nodo;
                private final double distancia;

                public NodeDistance(
                                Long nodo,
                                double distancia) {

                        this.nodo = nodo;
                        this.distancia = distancia;
                }

                public Long getNodo() {
                        return nodo;
                }

                public double getDistancia() {
                        return distancia;
                }
        }
}