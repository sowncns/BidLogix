package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProductItemCreateRequest {

    @NotBlank
    private String code;

    @NotNull
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("manufacturing_date")
    private Instant manufacturingDate;
}
