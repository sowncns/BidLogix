package com.qs.Backend.modules.crm.lead.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeadCreateRequest {
    private String name;
    private String phone;
    private String email;
    @JsonProperty("business_field")
    private String businessField;
    private String notes;
    private List<String> services;
}
