package com.qs.Backend.platform.auth.service;
import com.qs.Backend.platform.auth.dto.*;

import com.qs.Backend.platform.auth.service.AuthInterface;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AuthInterface {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
    TokenResponse refresh(String refreshToken);
    void logout(String accessToken) ;
    void changePassword(UUID accountId, ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    SSOTicketResponse issueSsoTicket(UUID accountId);
    LoginResponse loginWithSsoTicket(String rawTicket, HttpServletRequest httpRequest);
    String registerCustomer(CustomerRegisterRequest request);
    LoginResponse verifyContact(VerifyContactRequest request, HttpServletRequest httpRequest);
    void resendVerificationCode(ResendVerificationRequest request);
    UserResponse getUserWithPermissions(UUID accountId);
 


}
