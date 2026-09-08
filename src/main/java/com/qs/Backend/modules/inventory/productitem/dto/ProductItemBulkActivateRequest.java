package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductItemBulkActivateRequest {

    @NotNull
    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("product_id")
    private UUID productId;

    @NotEmpty
    private List<String> inputs;

    @JsonProperty("re_activate")
    private boolean reActivate;

    @JsonProperty("re_activate_reason")
    private String reActivateReason;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
}
