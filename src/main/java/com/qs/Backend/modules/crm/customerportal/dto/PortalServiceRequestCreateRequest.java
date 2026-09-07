package com.qs.Backend.modules.crm.customerportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortalServiceRequestCreateRequest {
    @JsonProperty("product_item_id")
    private String productItemId;
    private String type;
    @JsonProperty("service_type")
    private String serviceType;
    private String priority;
    private String description;
}
