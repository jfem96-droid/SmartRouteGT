package com.smartroutegt.backend.dto.routing;

import java.util.List;

public class RouteGeometryResponse {

    private Long routeId;
    private double distanceMeters;
    private double durationSeconds;
    private List<RouteGeometryPoint> points;

    public RouteGeometryResponse() {
    }

    public RouteGeometryResponse(
            Long routeId,
            double distanceMeters,
            double durationSeconds,
            List<RouteGeometryPoint> points) {

        this.routeId = routeId;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.points = points;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<RouteGeometryPoint> getPoints() {
        return points;
    }

    public void setPoints(List<RouteGeometryPoint> points) {
        this.points = points;
    }
}
