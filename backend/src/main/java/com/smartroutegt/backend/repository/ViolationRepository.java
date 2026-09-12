package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Violation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
}