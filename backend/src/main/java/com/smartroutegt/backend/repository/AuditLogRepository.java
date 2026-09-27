package com.smartroutegt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}