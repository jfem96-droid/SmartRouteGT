package com.smartroutegt.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.PasswordResetCode;

public interface PasswordResetCodeRepository
        extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode>
            findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}
