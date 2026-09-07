package com.qs.Backend.modules.crm.customerportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortalServiceRequestResponse {
    private String id;
    @JsonProperty("order_number")
    private String orderNumber;
    @JsonProperty("product_item_id")
    private String productItemId;
    @JsonProperty("asset_serial")
    private String assetSerial;
    private String type;
    @JsonProperty("service_type")
    private String serviceType;
    private String priority;
    private String description;
    private String status;
    @JsonProperty("service_logs")
    private List<Object> serviceLogs;
    private List<Object> attachments;
    @JsonProperty("created_at")
    private Instant createdAt;
}
