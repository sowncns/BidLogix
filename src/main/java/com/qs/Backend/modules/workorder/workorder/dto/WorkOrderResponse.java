package com.qs.Backend.modules.workorder.workorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WorkOrderResponse {
    private String id;
    @JsonProperty("order_number")
    private String orderNumber;
    @JsonProperty("product_item_id")
    private String productItemId;
    private String type;
    private String priority;
    @JsonProperty("assigned_to")
    private String assignedTo;
    private String description;
    private String status;
    @JsonProperty("scheduled_date")
    private Instant scheduledDate;
    @JsonProperty("started_at")
    private Instant startedAt;
    @JsonProperty("completed_at")
    private Instant completedAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("deleted_at")
    private Instant deletedAt;
}
