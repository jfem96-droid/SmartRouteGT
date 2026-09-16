package com.smartroutegt.backend.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Violation;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
    boolean existsByTripIdAndTipoAndFechaHoraAfter(
            Long tripId,
            String tipo,
            LocalDateTime fechaHora);
}