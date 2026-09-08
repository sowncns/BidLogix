package com.qs.Backend.platform.role.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoleListResponse {
    private List<RoleResponse> items;
    private long total;
    private int limit;
    private int offset;
}
