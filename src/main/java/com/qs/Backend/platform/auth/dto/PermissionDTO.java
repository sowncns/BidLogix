package com.qs.Backend.platform.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PermissionDTO {
    private UUID id;
    private String code;
    private String description;
}
