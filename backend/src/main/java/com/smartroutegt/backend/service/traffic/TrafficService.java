package com.smartroutegt.backend.service.traffic;

public interface TrafficService {

    TrafficEstimate estimate(
            Long tripId,
            double velocidadActualKmh
    );
}
