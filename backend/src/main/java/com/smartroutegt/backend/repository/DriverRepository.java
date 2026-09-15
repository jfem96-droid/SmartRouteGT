package com.smartroutegt.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByLicencia(String licencia);

    boolean existsByLicenciaAndIdNot(String licencia, Long id);

    Optional<Driver> findByLicencia(String licencia);
}