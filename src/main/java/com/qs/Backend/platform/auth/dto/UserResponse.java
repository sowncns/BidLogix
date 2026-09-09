package com.qs.Backend.platform.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qs.Backend.platform.user.dto.EmailNotificationPrefsResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String phone;

    @JsonProperty("full_name")
    private String fullName;

    private String region;

    @JsonProperty("ref_id")
    private UUID refId;

    @JsonProperty("is_active")
    private boolean active;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    private List<RoleDTO> roles;
    private List<PermissionDTO> permissions;

    @JsonProperty("data_scope")
    private String dataScope;

    @JsonProperty("organization_id")
    private UUID organizationId;

    @JsonProperty("email_notifications")
    private EmailNotificationPrefsResponse emailNotifications;
}
