package com.smartroutegt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);
}