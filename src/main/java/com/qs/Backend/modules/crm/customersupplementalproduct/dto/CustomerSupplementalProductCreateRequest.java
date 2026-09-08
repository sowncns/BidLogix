package com.qs.Backend.modules.crm.customersupplementalproduct.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CustomerSupplementalProductCreateRequest {
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_item_code")
    private String productItemCode;
    @JsonProperty("chip_id")
    private String chipId;
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
}
