package com.qs.Backend.modules.crm.lead.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class LeadResponse {
    private String id;
    private String name;
    private String phone;
    private String email;
    @JsonProperty("business_field")
    private String businessField;
    private String notes;
    private List<String> services;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
