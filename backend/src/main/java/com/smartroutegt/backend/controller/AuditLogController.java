package com.smartroutegt.backend.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<AuditLog> listarAuditoria() {
        return auditLogRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        AuditLog::getFechaHora,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
