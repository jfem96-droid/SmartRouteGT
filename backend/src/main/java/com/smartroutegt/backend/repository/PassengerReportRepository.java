package com.smartroutegt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.PassengerReport;

public interface PassengerReportRepository
        extends JpaRepository<PassengerReport, Long> {

}