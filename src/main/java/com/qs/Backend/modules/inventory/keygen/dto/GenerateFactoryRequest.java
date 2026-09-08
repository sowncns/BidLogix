package com.qs.Backend.modules.inventory.keygen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateFactoryRequest {
    @Min(0) @Max(59)
    private int minute;
    @Min(0) @Max(23)
    private int hour;
    @Min(1) @Max(31)
    private int day;
    @Min(1) @Max(12)
    private int month;
    @Min(2000) @Max(2100)
    private int year;
}
