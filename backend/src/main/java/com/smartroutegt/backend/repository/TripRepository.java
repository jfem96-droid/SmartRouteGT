package com.smartroutegt.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByDriverIdAndActivoTrue(Long driverId);

    Optional<Trip> findByDriverIdAndActivoTrue(Long driverId);
}