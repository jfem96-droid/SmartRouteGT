package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.dto.routing.RouteGeometryResponse;
import com.smartroutegt.backend.service.RouteGeometryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteGeometryController {

    private final RouteGeometryService routeGeometryService;

    public RouteGeometryController(
            RouteGeometryService routeGeometryService) {

        this.routeGeometryService = routeGeometryService;
    }

    @GetMapping("/{routeId}/geometry")
    public ResponseEntity<RouteGeometryResponse> getGeometry(
            @PathVariable Long routeId,
            @RequestParam(required = false) Long originStopId,
            @RequestParam(required = false) Long destinationStopId) {

        return ResponseEntity.ok(
                routeGeometryService.getRouteGeometry(
                        routeId,
                        originStopId,
                        destinationStopId
                )
        );
    }
}
