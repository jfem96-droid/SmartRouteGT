package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Violation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ViolationRepository
        extends JpaRepository<Violation, Long> {

    boolean existsByTripIdAndTipoAndFechaHoraAfter(
            Long tripId,
            String tipo,
            LocalDateTime fechaHora);
}