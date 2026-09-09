package com.qs.Backend.platform.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RoleDTO {
    private UUID id;
    private String code;
    private String name;

    @JsonProperty("data_scope")
    private String dataScope;
}
