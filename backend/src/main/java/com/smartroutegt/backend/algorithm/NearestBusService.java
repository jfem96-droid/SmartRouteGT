package com.smartroutegt.backend.algorithm;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.repository.BusLocationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NearestBusService {

    private final BusLocationRepository busLocationRepository;
    private final GraphService graphService;

    public NearestBusService(
            BusLocationRepository busLocationRepository,
            GraphService graphService) {

        this.busLocationRepository = busLocationRepository;
        this.graphService = graphService;
    }

    public BusLocation encontrarBusMasCercano(
            double latitudPasajero,
            double longitudPasajero) {

        List<BusLocation> ubicaciones =
                busLocationRepository.findLatestLocationsForAllBuses();

        if (ubicaciones.isEmpty()) {
            return null;
        }

        BusLocation busMasCercano = null;
        double menorDistancia = Double.MAX_VALUE;

        for (BusLocation ubicacion : ubicaciones) {

            double distancia =
                    graphService.calcularDistancia(
                            latitudPasajero,
                            longitudPasajero,
                            ubicacion.getLatitud(),
                            ubicacion.getLongitud()
                    );

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                busMasCercano = ubicacion;
            }
        }

        return busMasCercano;
    }
}