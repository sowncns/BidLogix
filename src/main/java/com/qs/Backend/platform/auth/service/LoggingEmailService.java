package com.qs.Backend.platform.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

// Fallback when no SMTP is configured (app.mail.enabled=false, the default): logs instead of
// sending. SmtpEmailService takes over once MAIL_ENABLED=true and SMTP credentials are set.
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailService implements EmailService {

    @Override
    public void sendPasswordResetEmail(String locale, String to, String resetLink) {
        log.info("[email:{}] Password reset link for {}: {}", locale, to, resetLink);
    }

    @Override
    public void sendPortalInviteEmail(String locale, String to, String inviteLink) {
        log.info("[email:{}] Portal invite link for {}: {}", locale, to, inviteLink);
    }

    @Override
    public void sendVerificationEmail(String locale, String to, String code, int expiryMinutes) {
        log.info("[email:{}] Verification code for {}: {} (expires in {} minutes)", locale, to, code, expiryMinutes);
    }
}
