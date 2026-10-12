package com.smartroutegt.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Violation;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
    boolean existsByTripIdAndTipoAndFechaHoraAfter(
            Long tripId,
            String tipo,
            LocalDateTime fechaHora);

    List<Violation> findByDriverIdOrderByFechaHoraDesc(Long driverId);

    boolean existsByDriverId(Long driverId);

    boolean existsByBusId(Long busId);

    long countByBusId(Long busId);
    long countByDriverId(Long driverId);
}