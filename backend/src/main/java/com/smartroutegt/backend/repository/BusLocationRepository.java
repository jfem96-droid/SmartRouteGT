package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.BusLocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusLocationRepository
        extends JpaRepository<BusLocation, Long> {

    List<BusLocation>
    findByTripIdOrderByFechaHoraAsc(Long tripId);

    Optional<BusLocation>
    findFirstByBusIdOrderByFechaHoraDesc(Long busId);

    Optional<BusLocation>
    findFirstByTripIdOrderByFechaHoraAsc(Long tripId);

    Optional<BusLocation>
    findFirstByTripIdAndVelocidadLessThanEqualOrderByFechaHoraDesc(
            Long tripId,
            double velocidad);

    Optional<BusLocation>
    findFirstByTripIdAndFechaHoraAfterOrderByFechaHoraAsc(
            Long tripId,
            java.time.LocalDateTime fechaHora);

    @Query("""
        SELECT bl
        FROM BusLocation bl
        WHERE bl.trip.activo = true
          AND bl.fechaHora = (
              SELECT MAX(bl2.fechaHora)
              FROM BusLocation bl2
              WHERE bl2.bus.id = bl.bus.id
                AND bl2.trip.activo = true
          )
    """)
    List<BusLocation> findLatestLocationsForAllBuses();
}