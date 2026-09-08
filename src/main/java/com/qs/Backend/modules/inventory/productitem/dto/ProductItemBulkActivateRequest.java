package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ProductItemBulkActivateRequest {

    @NotBlank
    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("product_id")
    private Long productId;

    @NotEmpty
    private List<String> inputs;

    @JsonProperty("re_activate")
    private boolean reActivate;

    @JsonProperty("re_activate_reason")
    private String reActivateReason;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
}
