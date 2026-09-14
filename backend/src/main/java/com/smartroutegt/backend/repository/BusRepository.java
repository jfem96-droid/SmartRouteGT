package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Bus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Long> {

    boolean existsByCodigo(String codigo);
    boolean existsByPlaca(String placa);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    boolean existsByPlacaAndIdNot(String placa, Long id);
}