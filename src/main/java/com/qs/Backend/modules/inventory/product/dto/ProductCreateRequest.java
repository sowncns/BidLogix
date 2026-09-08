package com.qs.Backend.modules.inventory.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ProductCreateRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private Map<String, Object> specifications = new LinkedHashMap<>();
    @JsonProperty("warranty_months")
    private Integer warrantyMonths;
}
