package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Driver;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByLicencia(String licencia);

    boolean existsByLicenciaAndIdNot(String licencia, Long id);
}