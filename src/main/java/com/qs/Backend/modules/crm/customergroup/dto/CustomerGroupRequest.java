package com.qs.Backend.modules.crm.customergroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerGroupRequest {
    private String name;
    private String description;
    @JsonProperty("label_vi")
    private String labelVi;
    @JsonProperty("label_en")
    private String labelEn;
}
