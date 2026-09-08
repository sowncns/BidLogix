package com.qs.Backend.platform.verification.service;

import com.qs.Backend.shared.exception.AppException;
import com.qs.Backend.platform.auth.security.TokenHasher;
import com.qs.Backend.platform.verification.entity.VerificationCode;
import com.qs.Backend.platform.verification.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    public static final int EXPIRY_MINUTES = 10;
    public static final int MAX_ATTEMPTS = 3;
    public static final int RATE_LIMIT_PER_HOUR = 5;

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateCode(String target, String purpose, UUID accountId) {
        target = normalizeTarget(target);
        long recentCount = verificationCodeRepository.countByTargetAndCreatedAtAfter(target, Instant.now().minus(1, ChronoUnit.HOURS));
        if (recentCount >= RATE_LIMIT_PER_HOUR) {
            throw new AppException("Bạn đã yêu cầu quá nhiều mã xác thực, vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION_RATE_LIMIT_EXCEEDED");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));

        VerificationCode entity = new VerificationCode();
        entity.setTarget(target);
        entity.setPurpose(purpose);
        entity.setAuthUserId(accountId);
        entity.setCode(code);
        entity.setCodeHash(TokenHasher.sha256(code));
        entity.setExpiresAt(Instant.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES));
        verificationCodeRepository.save(entity);

        return code;
    }

    @Transactional
    public void verifyCode(String target, String purpose, String code) {
        target = normalizeTarget(target);
        VerificationCode entity = verificationCodeRepository
                .findTopByTargetAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(target, purpose)
                .orElseThrow(() -> new AppException("Mã xác thực không hợp lệ", HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_INVALID"));

        if (entity.isExpired()) {
            throw new AppException("Mã xác thực đã hết hạn", HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_EXPIRED");
        }
        if (!entity.canAttempt(MAX_ATTEMPTS)) {
            throw new AppException("Bạn đã nhập sai mã xác thực quá số lần cho phép", HttpStatus.BAD_REQUEST, "VERIFICATION_MAX_ATTEMPTS_EXCEEDED");
        }
        if (!entity.getCode().equals(code)) {
            entity.setAttempts(entity.getAttempts() + 1);
            verificationCodeRepository.save(entity);
            if (!entity.canAttempt(MAX_ATTEMPTS)) {
                throw new AppException("Bạn đã nhập sai mã xác thực quá số lần cho phép", HttpStatus.BAD_REQUEST, "VERIFICATION_MAX_ATTEMPTS_EXCEEDED");
            }
            throw new AppException("Mã xác thực không đúng", HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_INVALID");
        }

        entity.setConsumedAt(Instant.now());
        verificationCodeRepository.save(entity);
    }

    private String normalizeTarget(String target) {
        return target == null ? null : target.trim().toLowerCase();
    }
}
