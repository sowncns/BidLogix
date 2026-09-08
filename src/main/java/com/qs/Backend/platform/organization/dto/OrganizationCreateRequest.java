package com.qs.Backend.platform.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrganizationCreateRequest {
    private String code;
    private String name;
    @JsonProperty("parent_id")
    private UUID parentId;
    private String path;
    private String status;
}
