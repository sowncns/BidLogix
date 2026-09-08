package com.qs.Backend.platform.permission.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {
    private Long id;
    private String code;
    private String description;
}
