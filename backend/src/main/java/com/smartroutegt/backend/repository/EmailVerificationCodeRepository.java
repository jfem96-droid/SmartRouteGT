package com.smartroutegt.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartroutegt.backend.entity.EmailVerificationCode;

public interface EmailVerificationCodeRepository
        extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode>
        findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}
