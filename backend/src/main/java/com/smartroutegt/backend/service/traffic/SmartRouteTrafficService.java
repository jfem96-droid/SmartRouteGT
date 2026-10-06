package com.smartroutegt.backend.service.traffic;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartroutegt.backend.entity.BusLocation;
import com.smartroutegt.backend.repository.BusLocationRepository;

@Service
public class SmartRouteTrafficService implements TrafficService {

    private static final double VELOCIDAD_REFERENCIA_KMH = 30.0;
    private static final double VELOCIDAD_MINIMA_ETA_KMH = 8.0;

    private final BusLocationRepository locationRepository;

    public SmartRouteTrafficService(
            BusLocationRepository locationRepository) {

        this.locationRepository = locationRepository;
    }

    @Override
    public TrafficEstimate estimate(
            Long tripId,
            double velocidadActualKmh) {

        List<BusLocation> historial =
                locationRepository.findByTripIdOrderByFechaHoraAsc(tripId);

        double velocidadPromedio = historial.stream()
                .mapToDouble(BusLocation::getVelocidad)
                .filter(v -> v >= 0.0)
                .average()
                .orElse(velocidadActualKmh);

        double velocidadObservada;

        if (velocidadActualKmh > 0.0 && velocidadPromedio > 0.0) {
            velocidadObservada =
                    (velocidadActualKmh * 0.70)
                    + (velocidadPromedio * 0.30);
        } else if (velocidadActualKmh > 0.0) {
            velocidadObservada = velocidadActualKmh;
        } else if (velocidadPromedio > 0.0) {
            velocidadObservada = velocidadPromedio;
        } else {
            velocidadObservada = 15.0;
        }

        String estado;

        if (velocidadObservada >= 25.0) {
            estado = "FLUIDO";
        } else if (velocidadObservada >= 15.0) {
            estado = "MODERADO";
        } else if (velocidadObservada >= 5.0) {
            estado = "PESADO";
        } else {
            estado = "MUY_PESADO";
        }

        double velocidadEstimada =
                Math.max(
                        VELOCIDAD_MINIMA_ETA_KMH,
                        velocidadObservada
                );

        double factorTrafico =
                VELOCIDAD_REFERENCIA_KMH / velocidadEstimada;

        return new TrafficEstimate(
                estado,
                redondear(VELOCIDAD_REFERENCIA_KMH),
                redondear(velocidadEstimada),
                redondear(factorTrafico),
                "SMARTROUTE_GPS"
        );
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
