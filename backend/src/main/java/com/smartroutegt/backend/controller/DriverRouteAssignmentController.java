package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.DriverRouteAssignment;
import com.smartroutegt.backend.repository.DriverRouteAssignmentRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
public class DriverRouteAssignmentController {

    private final DriverRouteAssignmentRepository driverRouteAssignmentRepository;

    public DriverRouteAssignmentController(DriverRouteAssignmentRepository driverRouteAssignmentRepository) {
        this.driverRouteAssignmentRepository = driverRouteAssignmentRepository;
    }

    @GetMapping
    public List<DriverRouteAssignment> obtenerAsignaciones() {
        return driverRouteAssignmentRepository.findAll();
    }
}