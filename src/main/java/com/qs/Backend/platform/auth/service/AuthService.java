package com.qs.Backend.platform.auth.service;

import com.qs.Backend.shared.exception.AppException;
import com.qs.Backend.platform.auth.dto.*;
import com.qs.Backend.platform.auth.entity.AuthUser;
import com.qs.Backend.platform.auth.entity.PasswordResetToken;
import com.qs.Backend.platform.auth.entity.Role;
import com.qs.Backend.platform.auth.entity.SSOTicket;
import com.qs.Backend.platform.auth.entity.Session;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import com.qs.Backend.platform.auth.repository.PasswordResetTokenRepository;
import com.qs.Backend.platform.auth.repository.RoleRepository;
import com.qs.Backend.platform.auth.repository.SSOTicketRepository;
import com.qs.Backend.platform.auth.repository.SessionRepository;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.auth.security.JwtService;
import com.qs.Backend.platform.auth.security.TokenHasher;
import com.qs.Backend.platform.permission.dto.UserPermissionInfo;
import com.qs.Backend.platform.permission.service.PermissionService;
import com.qs.Backend.platform.verification.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final long SESSION_TTL_MS = 30L * 24 * 60 * 60 * 1000;
    private static final long SSO_TICKET_TTL_SECONDS = 60;
    private static final long RESET_TOKEN_TTL_DAYS = 7;
    private static final String REGISTRATION_PURPOSE = "registration";

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SSOTicketRepository ssoTicketRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordPolicy passwordPolicy;
    private final PermissionService permissionService;
    private final VerificationService verificationService;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    // ---- Registration (internal/staff) ----

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username đã tồn tại", HttpStatus.CONFLICT, "USERNAME_TAKEN");
        }
        passwordPolicy.validate(request.getPassword(), request.getPassword());

        Set<Role> roles = resolveRoles(request.getRoleCodes());

        AuthUser account = new AuthUser();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEmail(request.getEmail());
        account.setRegion(request.getRegion() == null || request.getRegion().isBlank() ? "VN" : request.getRegion());
        account.setRoles(roles);
        account.setActive(true);
        accountRepository.save(account);

        // No auto-login: the caller must call /auth/login separately with the credentials just set.
        return new RegisterResponse(account.getId(), account.getUsername(), account.getEmail());
    }

    private Set<Role> resolveRoles(List<String> roleCodes) {
        return roleCodes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new AppException("Role không tồn tại: " + code, HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND")))
                .collect(Collectors.toSet());
    }

    // ---- Login / session lifecycle ----

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        AuthUser account = accountRepository.findActiveByUsernameOrEmailOrPhone(request.getUsername())
                .orElseThrow(() -> new AppException("Sai tài khoản hoặc mật khẩu", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new AppException("Sai tài khoản hoặc mật khẩu", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        return toLoginResponse(issueTokens(account, httpRequest));
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        if (username == null || !jwtService.isTokenValid(refreshToken, username)) {
            throw new AppException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        }

        Session session = sessionRepository.findByRefreshTokenHash(TokenHasher.sha256(refreshToken))
                .orElseThrow(() -> new AppException("Refresh token không hợp lệ", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN"));

        if (session.isRevoked() || session.isExpired()) {
            throw new AppException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED");
        }

        // Rotation: revoke the old session row, issue a brand new access+refresh pair
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);

        return issueTokens(session.getAccount(), null);
    }

    @Transactional
    public void logout(String accessToken) {
        String jti = jwtService.extractJti(accessToken);
        if (jti == null) return;
        sessionRepository.findByAccessTokenJti(jti).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    // ---- Password management ----

    @Transactional
    public void changePassword(Long accountId, ChangePasswordRequest request) {
        passwordPolicy.validate(request.getNewPassword(), request.getConfirmPassword());

        AuthUser account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException("Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new AppException("Mật khẩu mới phải khác mật khẩu cũ", HttpStatus.BAD_REQUEST, "PASSWORD_SAME_AS_OLD");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        sessionRepository.revokeAllForAccount(accountId, Instant.now());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Anti-enumeration: caller always returns the same success message regardless of outcome here.
        accountRepository.findActiveByUsernameOrEmailOrPhone(request.getUsername()).ifPresent(account -> {
            String rawToken = generateRandomToken();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setAccount(account);
            resetToken.setTokenHash(TokenHasher.sha256(rawToken));
            resetToken.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL_DAYS, ChronoUnit.DAYS));
            passwordResetTokenRepository.save(resetToken);

            String locale = LocaleResolver.fromRegion(account.getRegion());
            emailService.sendPasswordResetEmail(locale, account.getEmail(), rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordPolicy.validate(request.getNewPassword(), request.getConfirmPassword());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256(request.getToken()))
                .orElseThrow(() -> new AppException("Token không hợp lệ", HttpStatus.BAD_REQUEST, "RESET_TOKEN_INVALID"));

        if (resetToken.isUsed()) {
            throw new AppException("Token đã được sử dụng", HttpStatus.BAD_REQUEST, "RESET_TOKEN_USED");
        }
        if (resetToken.isExpired()) {
            throw new AppException("Token đã hết hạn", HttpStatus.BAD_REQUEST, "RESET_TOKEN_EXPIRED");
        }

        AuthUser account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        sessionRepository.revokeAllForAccount(account.getId(), Instant.now());
    }

    // ---- SSO ticket handoff ----

    @Transactional
    public SSOTicketResponse issueSsoTicket(Long accountId) {
        AuthUser account = accountRepository.findById(accountId)
                .filter(AuthUser::isActive)
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại hoặc đã bị khóa", HttpStatus.UNAUTHORIZED, "USER_INACTIVE"));

        String rawTicket = generateRandomToken();

        SSOTicket ticket = new SSOTicket();
        ticket.setAccount(account);
        ticket.setTokenHash(TokenHasher.sha256(rawTicket));
        ticket.setExpiresAt(Instant.now().plusSeconds(SSO_TICKET_TTL_SECONDS));
        ssoTicketRepository.save(ticket);

        return new SSOTicketResponse(rawTicket, SSO_TICKET_TTL_SECONDS);
    }

    @Transactional
    public LoginResponse loginWithSsoTicket(String rawTicket, HttpServletRequest httpRequest) {
        SSOTicket ticket = ssoTicketRepository.findByTokenHash(TokenHasher.sha256(rawTicket))
                .orElseThrow(() -> new AppException("SSO ticket không hợp lệ", HttpStatus.UNAUTHORIZED, "SSO_TICKET_INVALID"));

        if (ticket.isUsed()) {
            throw new AppException("SSO ticket đã được sử dụng", HttpStatus.UNAUTHORIZED, "SSO_TICKET_USED");
        }
        if (ticket.isExpired()) {
            throw new AppException("SSO ticket đã hết hạn", HttpStatus.UNAUTHORIZED, "SSO_TICKET_EXPIRED");
        }

        // Mark used first (at-most-once) before touching the session, to close the replay window.
        ticket.setUsedAt(Instant.now());
        ssoTicketRepository.save(ticket);

        AuthUser account = ticket.getAccount();
        if (!account.isActive()) {
            throw new AppException("Tài khoản đã bị khóa", HttpStatus.FORBIDDEN, "USER_INACTIVE");
        }

        return toLoginResponse(issueTokens(account, httpRequest));
    }

    // ---- Customer self-registration + contact verification ----

    @Transactional
    public String registerCustomer(CustomerRegisterRequest request) {
        passwordPolicy.validate(request.getPassword(), request.getPassword());
        if (!isEmail(request.getEmail())) {
            throw new AppException("Email không hợp lệ", HttpStatus.BAD_REQUEST, "INVALID_EMAIL");
        }
        if (accountRepository.findAnyStatusByUsernameOrEmailOrPhone(request.getEmail()).isPresent()) {
            throw new AppException("Email đã được đăng ký", HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
        }

        Role customerRole = roleRepository.findByCode(CUSTOMER_ROLE)
                .orElseThrow(() -> new AppException("Role CUSTOMER chưa được khởi tạo", HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_NOT_FOUND"));

        AuthUser account = new AuthUser();
        account.setUsername(request.getEmail());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRegion(request.getRegion() == null || request.getRegion().isBlank() ? "VN" : request.getRegion());
        account.setRoles(Set.of(customerRole));
        account.setActive(false); // activated once email is verified
        accountRepository.save(account);

        String code = verificationService.generateCode(request.getEmail(), REGISTRATION_PURPOSE, account.getId());
        String locale = LocaleResolver.fromRegion(account.getRegion());
        emailService.sendVerificationEmail(locale, account.getEmail(), code, VerificationService.EXPIRY_MINUTES);

        return code; // returned so the controller can fan the same code out to phone channels
    }

    @Transactional
    public LoginResponse verifyContact(VerifyContactRequest request, HttpServletRequest httpRequest) {
        verificationService.verifyCode(request.getEmail(), REGISTRATION_PURPOSE, request.getCode());

        AuthUser account = accountRepository.findAnyStatusByUsernameOrEmailOrPhone(request.getEmail())
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        account.setEmailVerifiedAt(Instant.now());
        account.setActive(true);
        accountRepository.save(account);

        return toLoginResponse(issueTokens(account, httpRequest));
    }

    @Transactional
    public void resendVerificationCode(ResendVerificationRequest request) {
        accountRepository.findAnyStatusByUsernameOrEmailOrPhone(request.getEmail()).ifPresent(account -> {
            String code = verificationService.generateCode(request.getEmail(), REGISTRATION_PURPOSE, account.getId());
            String locale = LocaleResolver.fromRegion(account.getRegion());
            emailService.sendVerificationEmail(locale, account.getEmail(), code, VerificationService.EXPIRY_MINUTES);
        });
        // Silently no-op when not found - avoids leaking which emails are registered.
    }

    // ---- Current-user / permissions ----

    public UserResponse getUserWithPermissions(Long accountId) {
        AuthUser account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        UserPermissionInfo info = permissionService.getUserPermissionInfo(accountId);

        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getPhone(),
                account.getRegion(),
                info.getRoles().stream().map(Role::getCode).toList(),
                info.getPermissions().stream().map(p -> p.getCode()).toList(),
                info.getDataScope(),
                info.getOrganizationIds()
        );
    }

    // ---- Internal helpers ----

    private TokenResponse issueTokens(AuthUser account, HttpServletRequest httpRequest) {
        AccountPrincipal principal = new AccountPrincipal(account);
        List<String> authorities = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(account.getUsername(), authorities, jti);
        String refreshToken = jwtService.generateRefreshToken(account.getUsername());

        Session session = new Session();
        session.setAccount(account);
        session.setRefreshTokenHash(TokenHasher.sha256(refreshToken));
        session.setAccessTokenJti(jti);
        session.setExpiresAt(Instant.now().plusMillis(SESSION_TTL_MS));
        if (httpRequest != null) {
            session.setDeviceInfo(httpRequest.getHeader("User-Agent"));
            session.setIpAddress(httpRequest.getRemoteAddr());
        }
        sessionRepository.save(session);

        return new TokenResponse(accessToken, refreshToken);
    }

    private LoginResponse toLoginResponse(TokenResponse tokens) {
        String username = jwtService.extractUsername(tokens.getAccessToken());
        AuthUser account = accountRepository.findByUsername(username).orElseThrow();
        return new LoginResponse(tokens, account.getId(), account.getUsername());
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private boolean isEmail(String value) {
        if (value == null) return false;
        int at = value.indexOf('@');
        if (at <= 0 || at == value.length() - 1) return false;
        if (value.contains(" ")) return false;
        int dot = value.indexOf('.', at);
        return dot > at + 1 && dot < value.length() - 1;
    }
}
