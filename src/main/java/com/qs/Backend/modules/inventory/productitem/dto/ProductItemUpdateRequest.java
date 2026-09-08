package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProductItemUpdateRequest {

    private String status;

    @JsonProperty("activation_notes")
    private String activationNotes;

    @JsonProperty("installation_date")
    private Instant installationDate;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
}
