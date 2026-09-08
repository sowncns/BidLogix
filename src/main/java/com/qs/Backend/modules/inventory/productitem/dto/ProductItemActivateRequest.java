package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ProductItemActivateRequest {

    @NotNull
    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("installation_date")
    private Instant installationDate;

    @JsonProperty("warranty_months")
    private Integer warrantyMonths;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;

    private String notes;
}
