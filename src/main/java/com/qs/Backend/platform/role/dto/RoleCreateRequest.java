package com.qs.Backend.platform.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qs.Backend.platform.permission.entity.DataScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCreateRequest {
    private String code;
    private String name;
    @JsonProperty("data_scope")
    private DataScope dataScope = DataScope.SELF;
}
