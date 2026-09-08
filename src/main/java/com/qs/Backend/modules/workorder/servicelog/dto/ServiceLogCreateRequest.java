package com.qs.Backend.modules.workorder.servicelog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ServiceLogCreateRequest {
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
}
