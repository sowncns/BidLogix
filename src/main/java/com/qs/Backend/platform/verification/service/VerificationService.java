package com.qs.Backend.platform.verification.service;

import com.qs.Backend.shared.exception.AppException;
import com.qs.Backend.platform.verification.entity.VerificationCode;
import com.qs.Backend.platform.verification.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {

    public static final int EXPIRY_MINUTES = 10;

    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateCode(String target, String purpose, Long accountId) {
        target = normalizeTarget(target);
        String code = String.format("%06d", random.nextInt(1_000_000));

        VerificationCode entity = new VerificationCode();
        entity.setTarget(target);
        entity.setPurpose(purpose);
        entity.setAccountId(accountId);
        entity.setCode(code);
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
        if (!entity.getCode().equals(code)) {
            throw new AppException("Mã xác thực không đúng", HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_INVALID");
        }

        entity.setConsumedAt(Instant.now());
        verificationCodeRepository.save(entity);
    }

    private String normalizeTarget(String target) {
        return target == null ? null : target.trim().toLowerCase();
    }
}
