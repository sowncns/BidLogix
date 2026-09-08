package com.qs.Backend.platform.permission.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PermissionListResponse {
    private List<PermissionResponse> items;
}
