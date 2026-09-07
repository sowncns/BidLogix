package com.qs.Backend.modules.crm.student.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCreateRequest {
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
}
