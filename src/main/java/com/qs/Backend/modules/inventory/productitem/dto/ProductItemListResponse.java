package com.qs.Backend.modules.inventory.productitem.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductItemListResponse {

    private List<ProductItemResponse> items;
    private int limit;
    private int offset;
    private long total;
}
