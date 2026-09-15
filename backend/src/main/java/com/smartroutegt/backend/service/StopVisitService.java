package com.smartroutegt.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartroutegt.backend.entity.Stop;
import com.smartroutegt.backend.entity.StopVisit;
import com.smartroutegt.backend.entity.Trip;
import com.smartroutegt.backend.repository.StopVisitRepository;

@Service
public class StopVisitService {

        private static final long TIEMPO_MINIMO_PARADA_SEGUNDOS = 5;

        private final StopVisitRepository stopVisitRepository;

        public StopVisitService(
                        StopVisitRepository stopVisitRepository) {

                this.stopVisitRepository = stopVisitRepository;
        }

        @Transactional
        public void procesarUbicacion(
                        Trip trip,
                        Stop stop,
                        double latitudBus,
                        double longitudBus,
                        LocalDateTime fechaHora) {

                if (!stop.isActiva()) {
                        return;
                }

                boolean yaCumplida = stopVisitRepository
                                .existsByTripIdAndStopIdAndCumplidaTrue(
                                                trip.getId(),
                                                stop.getId());

                if (yaCumplida) {
                        return;
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

                if (distancia > stop.getRadioPermitido()) {

                        if (visitaPendiente.isPresent()) {
                                stopVisitRepository.delete(visitaPendiente.get());
                        }

                        return;
                }

                if (visitaPendiente.isEmpty()) {

                        StopVisit nuevaVisita = new StopVisit(
                                        trip,
                                        stop,
                                        fechaHora,
                                        distancia);

                        stopVisitRepository.save(nuevaVisita);

                        return;
                }

                StopVisit visita = visitaPendiente.get();

                if (distancia < visita.getDistanciaMinima()) {
                        visita.setDistanciaMinima(distancia);
                }

                long segundosDentro = Duration.between(
                                visita.getFechaEntrada(),
                                fechaHora).getSeconds();

                if (segundosDentro >= TIEMPO_MINIMO_PARADA_SEGUNDOS) {

                        visita.setCumplida(true);
                        visita.setFechaCumplimiento(fechaHora);
                }

                stopVisitRepository.save(visita);
        }

        public double calcularDistanciaHaversine(
                        double latitud1,
                        double longitud1,
                        double latitud2,
                        double longitud2) {

                final double RADIO_TIERRA_METROS = 6371000.0;

                double lat1Rad = Math.toRadians(latitud1);
                double lat2Rad = Math.toRadians(latitud2);

                double diferenciaLatitud = Math.toRadians(latitud2 - latitud1);

                double diferenciaLongitud = Math.toRadians(longitud2 - longitud1);

                double a = Math.sin(diferenciaLatitud / 2)
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
