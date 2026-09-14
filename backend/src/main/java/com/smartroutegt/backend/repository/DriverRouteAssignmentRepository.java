package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.DriverRouteAssignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRouteAssignmentRepository
        extends JpaRepository<DriverRouteAssignment, Long> {

    List<DriverRouteAssignment> findByDriverId(Long driverId);

    boolean existsByDriverIdAndActivaTrue(Long driverId);

    boolean existsByBusIdAndActivaTrue(Long busId);
}