package com.qs.Backend.modules.notification.notification.controller;

import com.qs.Backend.modules.notification.notification.dto.*;
import com.qs.Backend.modules.notification.notification.service.NotificationService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<NotificationListResponse> list(@AuthenticationPrincipal AccountPrincipal principal, @RequestParam(defaultValue = "false") boolean unread, @RequestParam(defaultValue = "20") int limit, @RequestParam(required = false) String cursor) {
        return ApiResponse.ok(notificationService.list(userId(principal), unread, limit, cursor), null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(notificationService.unreadCount(userId(principal)), null);
    }

    @PostMapping
    public ApiResponse<NotificationResponse> create(@RequestBody NotificationCreateRequest request) {
        return ApiResponse.created(notificationService.create(request), "Notification created");
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable String id, @AuthenticationPrincipal AccountPrincipal principal) {
        notificationService.markRead(id, userId(principal));
        return ApiResponse.ok(null, "Notification marked read");
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal AccountPrincipal principal) {
        notificationService.markAllRead(userId(principal));
        return ApiResponse.ok(null, "Notifications marked read");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id, @AuthenticationPrincipal AccountPrincipal principal) {
        notificationService.delete(id, userId(principal));
        return ApiResponse.ok(null, "Notification deleted");
    }

    @DeleteMapping
    public ApiResponse<Void> deleteAll(@AuthenticationPrincipal AccountPrincipal principal) {
        notificationService.deleteAll(userId(principal));
        return ApiResponse.ok(null, "Notifications deleted");
    }

    private String userId(AccountPrincipal principal) {
        return principal == null ? null : String.valueOf(principal.getId());
    }
}
