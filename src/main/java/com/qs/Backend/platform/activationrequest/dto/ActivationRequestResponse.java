package com.qs.Backend.platform.activationrequest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qs.Backend.modules.inventory.productitem.dto.ProductItemBulkActivateResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ActivationRequestResponse {

    private UUID id;

    @JsonProperty("request_type")
    private String requestType;

    @JsonProperty("sales_user_id")
    private UUID salesUserId;

    @JsonProperty("customer_id")
    private UUID customerId;

    @JsonProperty("organization_id")
    private UUID organizationId;
    private List<String> inputs;

    @JsonProperty("warranty_expiry")
    private Instant warrantyExpiry;
    private String status;
    private ProductItemBulkActivateResponse result;

    @JsonProperty("reject_reason")
    private String rejectReason;

    @JsonProperty("submitted_at")
    private Instant submittedAt;

    @JsonProperty("reviewed_by")
    private UUID reviewedBy;

    @JsonProperty("reviewed_at")
    private Instant reviewedAt;
}
