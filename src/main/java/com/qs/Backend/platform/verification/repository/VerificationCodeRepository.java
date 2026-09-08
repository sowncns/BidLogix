package com.qs.Backend.platform.verification.repository;

import com.qs.Backend.platform.verification.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findTopByTargetAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(String target, String purpose);
    long countByTargetAndCreatedAtAfter(String target, Instant since);
}
