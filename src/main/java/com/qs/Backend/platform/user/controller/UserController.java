package com.qs.Backend.platform.user.controller;

import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.user.dto.*;
import com.qs.Backend.platform.user.service.UserAdminService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtTo,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String permissionCode,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        UserFilterParams filters = new UserFilterParams(keyword, createdAtFrom, createdAtTo, roleCode, permissionCode);
        return ApiResponse.ok(userAdminService.list(filters, limit, offset), null);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request,
                                             @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.created(userAdminService.create(request, principal.getId()), "User created");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(userAdminService.getById(id), null);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request,
                                             @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(userAdminService.update(id, request, principal.getId()), "User updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AccountPrincipal principal) {
        userAdminService.delete(id, principal.getId());
        return ApiResponse.ok(null, "User deactivated");
    }

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody UserChangePasswordRequest request,
                                             @AuthenticationPrincipal AccountPrincipal principal) {
        userAdminService.changePassword(id, request, principal.getId());
        return ApiResponse.ok(null, "Password changed");
    }

    @PatchMapping("/{id}/email-notifications")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> updateEmailNotifications(@PathVariable UUID id, @RequestBody UpdateEmailNotificationsRequest request) {
        return ApiResponse.ok(userAdminService.updateEmailNotifications(id, request), null);
    }

    // Self-service route: caller updates their own notification prefs without USER_MANAGE.
    @PatchMapping("/me/email-notifications")
    public ApiResponse<UserResponse> updateMyEmailNotifications(@RequestBody UpdateEmailNotificationsRequest request,
                                                                  @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(userAdminService.updateEmailNotifications(principal.getId(), request), null);
    }
}
