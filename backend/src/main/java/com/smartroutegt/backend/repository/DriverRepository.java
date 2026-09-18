package com.smartroutegt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Driver findByLicencia(String licencia);

}