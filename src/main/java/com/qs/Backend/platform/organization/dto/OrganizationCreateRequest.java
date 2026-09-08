package com.qs.Backend.platform.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationCreateRequest {
    private String code;
    private String name;
    @JsonProperty("parent_id")
    private Long parentId;
    private String path;
    private String status;
}
