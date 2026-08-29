package com.qs.Backend.platform.verification.repository;

import com.qs.Backend.platform.verification.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findTopByTargetAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(String target, String purpose);
}
