package com.qs.Backend.modules.inventory.productitem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemBulkRowResponse {

    private String input;
    private String status;
    private String error;

    @JsonProperty("product_id")
    private UUID productId;

    @JsonProperty("product_item_id")
    private UUID productItemId;

    @JsonProperty("active_key")
    private String activeKey;

    @JsonProperty("warranty_until")
    private String warrantyUntil;

    @JsonProperty("re_activate_reason")
    private String reActivateReason;
}
