package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Trip;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByDriverIdAndActivoTrue(Long driverId);

    Optional<Trip> findByDriverIdAndActivoTrue(Long driverId);
}