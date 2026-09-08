package com.qs.Backend.platform.profile.controller;

import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.profile.dto.ProfileResponse;
import com.qs.Backend.platform.profile.dto.ProfileUpdateRequest;
import com.qs.Backend.platform.profile.service.ProfileService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> me(@AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(profileService.getMe(principal == null ? null : principal.getId()), null);
    }

    @PatchMapping("/me")
    public ApiResponse<ProfileResponse> updateMe(@AuthenticationPrincipal AccountPrincipal principal, @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateMe(principal == null ? null : principal.getId(), request), "Profile updated");
    }
}
