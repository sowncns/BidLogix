package com.qs.Backend.platform.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qs.Backend.platform.permission.entity.DataScope;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    @JsonProperty("data_scope")
    private DataScope dataScope;
    private List<PermissionResponse> permissions;
}
