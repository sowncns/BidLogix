package com.qs.Backend.modules.crm.customerstatus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerStatusResponse {
    private String id;
    private String name;
    private String description;
    @JsonProperty("label_vi")
    private String labelVi;
    @JsonProperty("label_en")
    private String labelEn;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
