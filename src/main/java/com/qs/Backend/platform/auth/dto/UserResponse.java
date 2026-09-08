package com.qs.Backend.platform.auth.dto;

import com.qs.Backend.platform.permission.entity.DataScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String phone;
    private String region;
    private List<String> roles;
    private List<String> permissions;
    private DataScope dataScope;
    private List<UUID> organizationIds;
}
