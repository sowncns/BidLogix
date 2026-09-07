package com.qs.Backend.modules.crm.student.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponse {
    private String id;
    @JsonProperty("full_name")
    private String fullName;
    private String email;
    private String phone;
    @JsonProperty("business_field")
    private String businessField;
    @JsonProperty("deposit_agreed")
    private boolean depositAgreed;
    private String status;
    private String notes;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
