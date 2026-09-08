package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.RouteStop;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRouteIdOrderByOrdenAsc(Long routeId);
}