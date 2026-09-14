package com.smartroutegt.backend.service;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.Violation;
import com.smartroutegt.backend.repository.AuditLogRepository;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.ViolationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViolationService {

    private static final int LIMITE_SANCIONES = 10;

    private final ViolationRepository violationRepository;
    private final DriverRepository driverRepository;
    private final AuditLogRepository auditLogRepository;

    public ViolationService(
            ViolationRepository violationRepository,
            DriverRepository driverRepository,
            AuditLogRepository auditLogRepository) {

        this.violationRepository = violationRepository;
        this.driverRepository = driverRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Violation confirmarInfraccion(Long violationId) {

        Violation violation = violationRepository.findById(violationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La infracción no existe"));

        if (violation.getEstado() != Violation.Estado.PENDING) {
            throw new IllegalStateException(
                    "La infracción ya fue procesada");
        }

        Driver driver = violation.getDriver();

        violation.setEstado(Violation.Estado.CONFIRMED);

        int nuevasSanciones =
                driver.getSancionesConfirmadas() + 1;

        driver.setSancionesConfirmadas(nuevasSanciones);

       if (nuevasSanciones >= LIMITE_SANCIONES && driver.isActivo()) {

    driver.setActivo(false);

            auditLogRepository.save(new AuditLog(
                    "DRIVER_SUSPENDED",
                    "DRIVER",
                    driver.getId(),
                    "SYSTEM",
                    "Piloto suspendido automáticamente al alcanzar "
                            + nuevasSanciones
                            + " sanciones confirmadas"));
        }

        driverRepository.save(driver);

        auditLogRepository.save(new AuditLog(
                "VIOLATION_CONFIRMED",
                "VIOLATION",
                violation.getId(),
                "SYSTEM",
                "Infracción confirmada para el piloto "
                        + driver.getNombre()));

        return violationRepository.save(violation);
    }

    @Transactional
    public Violation rechazarInfraccion(Long violationId) {

        Violation violation = violationRepository.findById(violationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La infracción no existe"));

        if (violation.getEstado() != Violation.Estado.PENDING) {
            throw new IllegalStateException(
                    "La infracción ya fue procesada");
        }

        violation.setEstado(Violation.Estado.REJECTED);

        auditLogRepository.save(new AuditLog(
                "VIOLATION_REJECTED",
                "VIOLATION",
                violation.getId(),
                "SYSTEM",
                "Infracción rechazada"));

        return violationRepository.save(violation);
    }
}