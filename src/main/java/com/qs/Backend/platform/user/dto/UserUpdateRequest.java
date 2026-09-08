package com.qs.Backend.platform.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

// Every field null = leave untouched (PATCH semantics).
@Getter
@Setter
public class UserUpdateRequest {
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String region;
    private UUID organizationId;
    private List<UUID> roleIds;
}
