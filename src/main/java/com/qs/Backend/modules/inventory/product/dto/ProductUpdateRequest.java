package com.qs.Backend.modules.inventory.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProductUpdateRequest {
    private String name;
    private Map<String, Object> specifications;
    @JsonProperty("warranty_months")
    private Integer warrantyMonths;
}
