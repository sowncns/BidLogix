package com.qs.Backend.modules.workorder.workorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class WorkOrderCreateRequest {
    @JsonProperty("product_item_id")
    private String productItemId;
    private String type;
    private String priority;
    @JsonProperty("assigned_to")
    private String assignedTo;
    private String description;
    @JsonProperty("scheduled_date")
    private Instant scheduledDate;
}
