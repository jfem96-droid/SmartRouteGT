package com.smartroutegt.backend.service;

import com.smartroutegt.backend.entity.Driver;
import com.smartroutegt.backend.entity.Violation;
import com.smartroutegt.backend.repository.DriverRepository;
import com.smartroutegt.backend.repository.ViolationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViolationService {

    private static final int LIMITE_SANCIONES = 10;

    private final ViolationRepository violationRepository;
    private final DriverRepository driverRepository;

    public ViolationService(
            ViolationRepository violationRepository,
            DriverRepository driverRepository) {
        this.violationRepository = violationRepository;
        this.driverRepository = driverRepository;
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

        if (nuevasSanciones >= LIMITE_SANCIONES) {
            driver.setActivo(false);
        }

        driverRepository.save(driver);

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

        return violationRepository.save(violation);
    }
}