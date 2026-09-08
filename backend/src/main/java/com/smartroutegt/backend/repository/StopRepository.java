package com.smartroutegt.backend.repository;

import com.smartroutegt.backend.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<Stop, Long> {
}