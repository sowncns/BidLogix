package com.qs.Backend.platform.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarUrl;
    private String region;
    private Long organizationId;
    private List<RoleInfo> roles;
    private List<PermissionInfo> permissions;
    private EmailNotificationPrefsResponse emailNotifications;
    private Instant createdAt;
}
