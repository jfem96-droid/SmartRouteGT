package com.smartroutegt.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.DriverRouteAssignment;

public interface DriverRouteAssignmentRepository
        extends JpaRepository<DriverRouteAssignment, Long> {

    List<DriverRouteAssignment> findByDriverId(Long driverId);

    List<DriverRouteAssignment> findByBusIdAndActivaTrue(Long busId);

}
