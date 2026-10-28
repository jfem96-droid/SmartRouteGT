package com.smartroutegt.backend.controller;

public class AssignmentRequest {

    private Long driverId;
    private Long busId;
    private Long routeId;
    private com.smartroutegt.backend.entity.DriverRouteAssignment.TipoRecorrido tipoRecorrido;

    public com.smartroutegt.backend.entity.DriverRouteAssignment.TipoRecorrido getTipoRecorrido() { return tipoRecorrido; }
    public void setTipoRecorrido(com.smartroutegt.backend.entity.DriverRouteAssignment.TipoRecorrido tipoRecorrido) { this.tipoRecorrido = tipoRecorrido; }


    public AssignmentRequest() {
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}