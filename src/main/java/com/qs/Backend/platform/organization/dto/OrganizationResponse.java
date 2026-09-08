package com.qs.Backend.platform.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class OrganizationResponse {
    private Long id;
    private String code;
    private String name;
    @JsonProperty("parent_id")
    private Long parentId;
    private String path;
    private Integer level;
    private String status;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
