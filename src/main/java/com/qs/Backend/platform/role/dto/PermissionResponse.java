package com.qs.Backend.platform.role.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PermissionResponse {
    private UUID id;
    private String code;
    private String description;
}
