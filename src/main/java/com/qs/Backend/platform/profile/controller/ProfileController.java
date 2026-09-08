package com.qs.Backend.platform.profile.controller;

import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.auth.dto.ChangePasswordRequest;
import com.qs.Backend.platform.auth.service.AuthService;
import com.qs.Backend.platform.file.entity.StoredFile;
import com.qs.Backend.platform.profile.dto.ProfileResponse;
import com.qs.Backend.platform.profile.dto.ProfileUpdateRequest;
import com.qs.Backend.platform.profile.service.ProfileService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/profile/avatar")
    public ApiResponse<ProfileResponse> uploadMyAvatar(@AuthenticationPrincipal AccountPrincipal principal,
                                                       @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(profileService.uploadAvatar(principal.getId(), file, principal.getId()), "Avatar uploaded");
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

    @PostMapping("/users/{id}/avatar")
    public ApiResponse<ProfileResponse> uploadUserAvatar(@PathVariable Long id,
                                                         @AuthenticationPrincipal AccountPrincipal principal,
                                                         @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(profileService.uploadAvatar(id, file, principal.getId()), "Avatar uploaded");
    }

    @GetMapping("/users/{id}/avatar/{fileId}")
    public ResponseEntity<Resource> downloadUserAvatar(@PathVariable Long id, @PathVariable String fileId) {
        StoredFile storedFile = profileService.findAvatarFile(id, fileId);
        MediaType mediaType = storedFile.getMimeType() != null
                ? MediaType.parseMediaType(storedFile.getMimeType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName(storedFile.getOriginalName()) + "\"")
                .body(profileService.loadAvatar(storedFile));
    }

    private static String safeName(String name) {
        return name != null ? name.replace("\"", "") : "avatar";
    }
}
