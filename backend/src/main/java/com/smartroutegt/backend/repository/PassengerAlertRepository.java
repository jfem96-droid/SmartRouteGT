package com.smartroutegt.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.PassengerAlert;

public interface PassengerAlertRepository extends JpaRepository<PassengerAlert, Long> {

    List<PassengerAlert> findByTipoAndEstadoOrderByFechaHoraDesc(
            PassengerAlert.Tipo tipo,
            PassengerAlert.Estado estado);
}
