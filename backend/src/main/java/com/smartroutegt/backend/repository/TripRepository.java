package com.smartroutegt.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByDriverIdAndActivoTrue(Long driverId);

    Optional<Trip> findByDriverIdAndActivoTrue(Long driverId);

    Optional<Trip> findByBusIdAndActivoTrue(Long busId);

    List<Trip> findByActivoTrue();

    boolean existsByDriverId(Long driverId);

    boolean existsByBusId(Long busId);

    boolean existsByRouteId(Long routeId);

    long countByBusId(Long busId);
    long countByDriverId(Long driverId);
    long countByRouteId(Long routeId);
}
