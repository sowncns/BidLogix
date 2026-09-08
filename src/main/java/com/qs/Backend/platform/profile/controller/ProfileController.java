package com.qs.Backend.platform.profile.controller;

import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.auth.dto.ChangePasswordRequest;
import com.qs.Backend.platform.auth.service.AuthService;
import com.qs.Backend.platform.profile.dto.ProfileResponse;
import com.qs.Backend.platform.profile.dto.ProfileUpdateRequest;
import com.qs.Backend.platform.profile.service.ProfileService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final AuthService authService;

    @GetMapping({"/profile", "/profiles/me"})
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(profileService.getMe(principal == null ? null : principal.getId()), null);
    }

    @PatchMapping({"/profile", "/profiles/me"})
    public ApiResponse<ProfileResponse> updateMe(@AuthenticationPrincipal AccountPrincipal principal, @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateMe(principal == null ? null : principal.getId(), request), "Profile updated");
    }

    @PostMapping("/profile/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal AccountPrincipal principal,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request);
        return ApiResponse.ok(null, "Đổi mật khẩu thành công");
    }

    @GetMapping("/users/{id}/profile")
    public ApiResponse<ProfileResponse> getUserProfile(@PathVariable Long id) {
        return ApiResponse.ok(profileService.getByUserId(id), null);
    }

    @PatchMapping("/users/{id}/profile")
    public ApiResponse<ProfileResponse> updateUserProfile(@PathVariable Long id,
                                                          @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateByUserId(id, request), "Profile updated");
    }
}
