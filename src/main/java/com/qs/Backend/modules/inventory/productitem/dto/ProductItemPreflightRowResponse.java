package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductItemPreflightRowResponse {

    private String input;

    @JsonProperty("chip_id")
    private String chipId;
    private String model;
    private String status;

    @JsonProperty("claimed_by_id")
    private Long claimedById;

    @JsonProperty("product_item_id")
    private Long productItemId;

    @JsonProperty("customer_id")
    private String customerId;
}
