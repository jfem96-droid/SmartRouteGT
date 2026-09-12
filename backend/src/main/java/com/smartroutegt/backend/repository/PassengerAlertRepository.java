package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.PassengerAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerAlertRepository extends JpaRepository<PassengerAlert, Long> {
}
