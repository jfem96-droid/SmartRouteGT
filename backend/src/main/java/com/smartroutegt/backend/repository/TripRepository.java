package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByDriverIdAndActivoTrue(Long driverId);
}