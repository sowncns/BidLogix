package com.qs.Backend.platform.auth.service;

// Dependency-inversion boundary for outbound email. Swap the default LoggingEmailService bean for
// a real SMTP/provider implementation once infra credentials are available.
public interface EmailService {
    void sendPasswordResetEmail(String locale, String to, String resetLink);
    void sendPortalInviteEmail(String locale, String to, String inviteLink);
    void sendVerificationEmail(String locale, String to, String code, int expiryMinutes);
}
