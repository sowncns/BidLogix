package com.qs.Backend.platform.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// Real SMTP implementation, active only when app.mail.enabled=true (MAIL_ENABLED env var) and
// spring.mail.* / MAIL_HOST etc are configured. Falls back to LoggingEmailService otherwise.
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    public void sendPasswordResetEmail(String locale, String to, String resetLink) {
        boolean vi = "vi".equals(locale);
        String subject = vi ? "Yêu cầu đặt lại mật khẩu" : "Password reset request";
        String body = vi
                ? "Mã/token đặt lại mật khẩu của bạn: " + resetLink + "\n\nLiên kết có hiệu lực trong 7 ngày."
                : "Your password reset token: " + resetLink + "\n\nThis link is valid for 7 days.";
        send(to, subject, body);
    }

    @Override
    public void sendPortalInviteEmail(String locale, String to, String inviteLink) {
        boolean vi = "vi".equals(locale);
        String subject = vi ? "Lời mời truy cập cổng thông tin" : "Portal access invitation";
        String body = vi
                ? "Bạn đã được cấp quyền truy cập cổng thông tin. Thiết lập mật khẩu tại: " + inviteLink
                : "You've been granted portal access. Set your password at: " + inviteLink;
        send(to, subject, body);
    }

    @Override
    public void sendVerificationEmail(String locale, String to, String code, int expiryMinutes) {
        boolean vi = "vi".equals(locale);
        String subject = vi ? "Mã xác thực tài khoản" : "Your verification code";
        String body = vi
                ? String.format("Mã xác thực của bạn là: %s (hết hạn sau %d phút).", code, expiryMinutes)
                : String.format("Your verification code is: %s (expires in %d minutes).", code, expiryMinutes);
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("[email] skip send, recipient address is empty (subject: {})", subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            log.error("[email] failed to send to {} (subject: {}): {}", to, subject, ex.getMessage());
        }
    }
}
