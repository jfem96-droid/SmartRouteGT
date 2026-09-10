package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.BusLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusLocationRepository
        extends JpaRepository<BusLocation, Long> {

    List<BusLocation> findByTripIdOrderByFechaHoraAsc(Long tripId);

    Optional<BusLocation> findFirstByBusIdOrderByFechaHoraDesc(Long busId);
}