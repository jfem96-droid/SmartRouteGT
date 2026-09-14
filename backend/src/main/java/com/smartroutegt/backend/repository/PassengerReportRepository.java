package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.PassengerReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerReportRepository
        extends JpaRepository<PassengerReport, Long> {

    List<PassengerReport> findByEstado(
            PassengerReport.Estado estado);

    List<PassengerReport> findByDriverId(Long driverId);
}