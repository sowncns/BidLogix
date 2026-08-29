package com.qs.Backend.platform.auth.controller;

import com.qs.Backend.shared.response.ApiResponse;
import com.qs.Backend.platform.auth.dto.*;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.auth.service.AuthService;
import com.qs.Backend.platform.auth.service.PhoneOtpDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PhoneOtpDispatcher phoneOtpDispatcher;

    // ---- Public ----

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.created(authService.register(request), "Đăng ký thành công, vui lòng đăng nhập");
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.login(request, httpRequest), "Đăng nhập thành công");
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestParam String refreshToken) {
        return ApiResponse.ok(authService.refresh(refreshToken), "Làm mới token thành công");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null, "Nếu tài khoản tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null, "Đặt lại mật khẩu thành công");
    }

    @PostMapping("/customer-register")
    public ApiResponse<CustomerRegisterResponse> customerRegister(@Valid @RequestBody CustomerRegisterRequest request) {
        String code = authService.registerCustomer(request);
        phoneOtpDispatcher.dispatch(null, request.getPhone(), code, 10);
        return ApiResponse.created(
                new CustomerRegisterResponse("Vui lòng kiểm tra email để xác thực tài khoản", maskEmail(request.getEmail())),
                "Đăng ký thành công"
        );
    }

    @PostMapping("/verify")
    public ApiResponse<LoginResponse> verify(@Valid @RequestBody VerifyContactRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.verifyContact(request, httpRequest), "Xác thực thành công");
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationCode(request);
        return ApiResponse.ok(null, "Nếu email tồn tại, mã xác thực mới đã được gửi");
    }

    @PostMapping("/sso")
    public ApiResponse<LoginResponse> ssoLogin(@Valid @RequestBody SSOLoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.loginWithSsoTicket(request.getTicket(), httpRequest), "Đăng nhập SSO thành công");
    }

    // ---- Protected ----

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(authService.getUserWithPermissions(principal.getId()), null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ApiResponse.ok(null, "Đăng xuất thành công");
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal AccountPrincipal principal,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ApiResponse.ok(null, "Đổi mật khẩu thành công");
    }

    @PostMapping("/sso/ticket")
    public ApiResponse<SSOTicketResponse> issueSsoTicket(@AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(authService.issueSsoTicket(principal.getId()), "Tạo SSO ticket thành công");
    }

    private String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
