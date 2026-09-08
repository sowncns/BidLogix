package com.qs.Backend.modules.workorder.servicelog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ServiceLogResponse {
    private String id;
    @JsonProperty("product_item_id")
    private String productItemId;
    @JsonProperty("work_order_id")
    private String workOrderId;
    @JsonProperty("service_date")
    private Instant serviceDate;
    @JsonProperty("service_type")
    private String serviceType;
    @JsonProperty("issue_description")
    private String issueDescription;
    @JsonProperty("action_taken")
    private String actionTaken;
    @JsonProperty("parts_replaced")
    private List<PartReplacedDto> partsReplaced;
    @JsonProperty("technician_id")
    private String technicianId;
    @JsonProperty("customer_notes")
    private String customerNotes;
    @JsonProperty("internal_notes")
    private String internalNotes;
    @JsonProperty("source_comment_id")
    private String sourceCommentId;
    @JsonProperty("event_type")
    private String eventType;
    @JsonProperty("event_metadata")
    private Map<String, Object> eventMetadata;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
