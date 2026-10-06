package com.smartroutegt.backend.dto.routing;

public class RouteGeometryPoint {

    private double latitud;
    private double longitud;

    public RouteGeometryPoint() {
    }

    public RouteGeometryPoint(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
