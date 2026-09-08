package com.qs.Backend.modules.inventory.keygen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MachineLockKeysResponse {
    private String week;
    @JsonProperty("days_30")
    private String days30;
    @JsonProperty("days_60")
    private String days60;
    @JsonProperty("year_1")
    private String year1;
    @JsonProperty("years_2")
    private String years2;
    private String infinity;
}
