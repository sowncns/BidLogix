package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProductItemActivateRequest {

    @NotBlank
    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("installation_date")
    private Instant installationDate;

    @JsonProperty("warranty_months")
    private Integer warrantyMonths;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;

    private String notes;
}
