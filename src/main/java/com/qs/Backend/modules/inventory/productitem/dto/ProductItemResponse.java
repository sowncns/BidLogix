package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ProductItemResponse {

    private UUID id;
    private String code;

    @JsonProperty("product_id")
    private UUID productId;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("manufacturing_date")
    private Instant manufacturingDate;

    @JsonProperty("installation_date")
    private Instant installationDate;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;

    private String status;

    @JsonProperty("activated_at")
    private Instant activatedAt;

    @JsonProperty("activated_by")
    private UUID activatedBy;

    @JsonProperty("activation_notes")
    private String activationNotes;

    @JsonProperty("recalled_at")
    private Instant recalledAt;

    @JsonProperty("recall_notes")
    private String recallNotes;

    @JsonProperty("refurbished_at")
    private Instant refurbishedAt;

    @JsonProperty("refurbish_notes")
    private String refurbishNotes;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("deleted_at")
    private Instant deletedAt;
}
