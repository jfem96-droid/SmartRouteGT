package com.smartroutegt.backend.repository;
import com.smartroutegt.backend.entity.DriverReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DriverReportRepository extends JpaRepository<DriverReport,Long> {
    List<DriverReport> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<DriverReport> findAllByOrderByCreatedAtDesc();
}
