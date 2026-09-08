package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.DriverRouteAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRouteAssignmentRepository
        extends JpaRepository<DriverRouteAssignment, Long> {
}