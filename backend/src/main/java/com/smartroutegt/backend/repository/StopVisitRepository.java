package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.StopVisit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StopVisitRepository
        extends JpaRepository<StopVisit, Long> {

    List<StopVisit> findByTripId(Long tripId);

    Optional<StopVisit>
    findByTripIdAndStopIdAndCumplidaFalse(
            Long tripId,
            Long stopId);

    boolean existsByTripIdAndStopIdAndCumplidaTrue(
            Long tripId,
            Long stopId);
}