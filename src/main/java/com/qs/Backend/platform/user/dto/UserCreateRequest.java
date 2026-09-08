package com.qs.Backend.platform.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserCreateRequest {
    private String email;
    private String phone;
    private String password;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String region;
    private UUID organizationId;
    private List<UUID> roleIds;
}
