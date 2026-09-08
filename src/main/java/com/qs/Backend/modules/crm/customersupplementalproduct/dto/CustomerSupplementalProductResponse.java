package com.qs.Backend.modules.crm.customersupplementalproduct.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CustomerSupplementalProductResponse {
    private Long id;
    private String code;
    @JsonProperty("customer_id")
    private String customerId;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
    private String status;
    @JsonProperty("activated_by")
    private Long activatedBy;
    @JsonProperty("activated_at")
    private Instant activatedAt;
    @JsonProperty("created_at")
    private Instant createdAt;
}
