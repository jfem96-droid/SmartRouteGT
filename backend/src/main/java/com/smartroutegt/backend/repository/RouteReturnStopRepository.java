package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.RouteReturnStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteReturnStopRepository extends JpaRepository<RouteReturnStop, Long> {
    List<RouteReturnStop> findByRouteIdOrderByOrdenAsc(Long routeId);
    void deleteByRouteId(Long routeId);
}
