package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.PassengerAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerAlertRepository extends JpaRepository<PassengerAlert, Long> {

    List<PassengerAlert> findByEstado(PassengerAlert.Estado estado);

}