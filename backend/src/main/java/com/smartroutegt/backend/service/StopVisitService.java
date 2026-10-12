package com.smartroutegt.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartroutegt.backend.dto.driver.DriverSafetyResponse;
import com.smartroutegt.backend.entity.RouteStop;
import com.smartroutegt.backend.entity.StopVisit;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.entity.Violation;
import com.smartroutegt.backend.repository.StopVisitRepository;
import com.smartroutegt.backend.repository.ViolationRepository;

@Service
public class StopVisitService {

    private static final double MARGEN_SALIDA_MINIMO_METROS = 20.0;

    private final StopVisitRepository stopVisitRepository;
    private final ViolationRepository violationRepository;

    public StopVisitService(
            StopVisitRepository stopVisitRepository,
            ViolationRepository violationRepository) {

        this.stopVisitRepository = stopVisitRepository;
        this.violationRepository = violationRepository;
    }

    @Transactional
    public DriverSafetyResponse.StopStatus procesarUbicacion(
            Trip trip,
            RouteStop routeStop,
            double latitudBus,
            double longitudBus,
            LocalDateTime fechaHora) {

        if (routeStop == null || routeStop.getStop() == null) {
            return null;
        }

        var stop = routeStop.getStop();

        if (!stop.isActiva()) {
            return null;
        }

        int tiempoMinimo = Math.max(
                routeStop.getTiempoMinimoSegundos(),
                1);

        boolean yaCumplida = stopVisitRepository
                .existsByTripIdAndStopIdAndCumplidaTrue(
                        trip.getId(),
                        stop.getId());

        if (yaCumplida) {
            return new DriverSafetyResponse.StopStatus(
                    stop.getId(),
                    stop.getNombre(),
                    "COMPLETED",
                    0.0,
                    tiempoMinimo);
        }

        double distancia = calcularDistanciaHaversine(
                latitudBus,
                longitudBus,
                stop.getLatitud(),
                stop.getLongitud());

        Optional<StopVisit> visitaPendiente = stopVisitRepository
                .findByTripIdAndStopIdAndCumplidaFalse(
                        trip.getId(),
                        stop.getId());

        double radio = stop.getRadioPermitido();

        double margenSalida = Math.max(
                MARGEN_SALIDA_MINIMO_METROS,
                radio * 0.50);

        double limiteSalidaConfirmada =
                radio + margenSalida;

        if (distancia > radio) {

            if (visitaPendiente.isEmpty()) {
                return new DriverSafetyResponse.StopStatus(
                        stop.getId(),
                        stop.getNombre(),
                        "APPROACHING",
                        distancia,
                        tiempoMinimo);
            }

            StopVisit visita = visitaPendiente.get();

            long segundosDentro = Math.max(
                    Duration.between(
                            visita.getFechaEntrada(),
                            fechaHora
                    ).getSeconds(),
                    0
            );

            if (segundosDentro >= tiempoMinimo) {

                visita.setCumplida(true);
                visita.setFechaCumplimiento(fechaHora);
                stopVisitRepository.save(visita);

                return new DriverSafetyResponse.StopStatus(
                        stop.getId(),
                        stop.getNombre(),
                        "COMPLETED",
                        distancia,
                        tiempoMinimo);
            }

            if (distancia <= limiteSalidaConfirmada) {
                return new DriverSafetyResponse.StopStatus(
                        stop.getId(),
                        stop.getNombre(),
                        "INSIDE",
                        distancia,
                        tiempoMinimo);
            }

            boolean infraccionExistente =
                    violationRepository
                            .existsByTripIdAndTipoAndFechaHoraAfter(
                                    trip.getId(),
                                    "STOP_SKIPPED",
                                    visita.getFechaEntrada());

            if (!infraccionExistente) {

                Violation violation = new Violation(
                        trip.getDriver(),
                        trip.getBus(),
                        trip,
                        "STOP_SKIPPED",
                        "Parada incumplida: "
                                + stop.getNombre()
                                + ". Permanencia registrada: "
                                + segundosDentro
                                + " s de "
                                + tiempoMinimo
                                + " s requeridos.",
                        null,
                        null);

                violationRepository.save(violation);
            }

            /*
             * Cerramos la visita para que el recorrido pueda continuar
             * hacia la siguiente parada.
             *
             * La infracción STOP_SKIPPED conserva el hecho de que
             * esta parada fue incumplida.
             */
            visita.setCumplida(true);
            visita.setFechaCumplimiento(fechaHora);
            stopVisitRepository.save(visita);

            return new DriverSafetyResponse.StopStatus(
                    stop.getId(),
                    stop.getNombre(),
                    "SKIPPED",
                    distancia,
                    tiempoMinimo);
        }

        if (visitaPendiente.isEmpty()) {

            StopVisit nuevaVisita = new StopVisit(
                    trip,
                    stop,
                    fechaHora,
                    distancia);

            stopVisitRepository.save(nuevaVisita);

            return new DriverSafetyResponse.StopStatus(
                    stop.getId(),
                    stop.getNombre(),
                    "INSIDE",
                    distancia,
                    tiempoMinimo);
        }

        StopVisit visita = visitaPendiente.get();

        if (distancia < visita.getDistanciaMinima()) {
            visita.setDistanciaMinima(distancia);
        }

        long segundosDentro = Math.max(
                Duration.between(
                        visita.getFechaEntrada(),
                        fechaHora
                ).getSeconds(),
                0
        );

        if (segundosDentro >= tiempoMinimo) {

            visita.setCumplida(true);
            visita.setFechaCumplimiento(fechaHora);

            stopVisitRepository.save(visita);

            return new DriverSafetyResponse.StopStatus(
                    stop.getId(),
                    stop.getNombre(),
                    "COMPLETED",
                    distancia,
                    tiempoMinimo);
        }

        stopVisitRepository.save(visita);

        return new DriverSafetyResponse.StopStatus(
                stop.getId(),
                stop.getNombre(),
                "INSIDE",
                distancia,
                tiempoMinimo);
    }

    public double calcularDistanciaHaversine(
            double latitud1,
            double longitud1,
            double latitud2,
            double longitud2) {

        final double RADIO_TIERRA_METROS = 6371000.0;

        double lat1Rad = Math.toRadians(latitud1);
        double lat2Rad = Math.toRadians(latitud2);

        double diferenciaLatitud =
                Math.toRadians(latitud2 - latitud1);

        double diferenciaLongitud =
                Math.toRadians(longitud2 - longitud1);

        double a =
                Math.sin(diferenciaLatitud / 2)
                        * Math.sin(diferenciaLatitud / 2)
                        +
                        Math.cos(lat1Rad)
                                * Math.cos(lat2Rad)
                                * Math.sin(diferenciaLongitud / 2)
                                * Math.sin(diferenciaLongitud / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a));

        return RADIO_TIERRA_METROS * c;
    }
}
