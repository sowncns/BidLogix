package com.qs.Backend.modules.crm.customerstatus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerStatusRequest {
    private String name;
    private String description;
    @JsonProperty("label_vi")
    private String labelVi;
    @JsonProperty("label_en")
    private String labelEn;
}
