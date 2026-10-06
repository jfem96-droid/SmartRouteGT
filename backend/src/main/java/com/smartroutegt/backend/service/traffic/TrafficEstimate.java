package com.smartroutegt.backend.service.traffic;

public record TrafficEstimate(
        String estado,
        double velocidadBaseKmh,
        double velocidadEstimadaKmh,
        double factorTrafico,
        String proveedor
) {
}
