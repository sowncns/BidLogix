package com.qs.Backend.platform.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignPermissionRequest {
    @JsonProperty("permission_code")
    private String permissionCode;
}
