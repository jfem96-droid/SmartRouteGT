package com.smartroutegt.backend.controller;

import com.smartroutegt.backend.entity.AuditLog;
import com.smartroutegt.backend.repository.AuditLogRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<AuditLog> obtenerAuditoria() {
        return auditLogRepository.findAll();
    }
}