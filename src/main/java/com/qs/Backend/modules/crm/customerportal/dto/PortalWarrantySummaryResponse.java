package com.qs.Backend.modules.crm.customerportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortalWarrantySummaryResponse {
    @JsonProperty("total_product_items")
    private int totalProductItems;
    @JsonProperty("active_warranties")
    private int activeWarranties;
    @JsonProperty("expiring_soon")
    private int expiringSoon;
    private int expired;
}
