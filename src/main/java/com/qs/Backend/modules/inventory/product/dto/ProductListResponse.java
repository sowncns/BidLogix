package com.qs.Backend.modules.inventory.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductListResponse {
    private List<ProductResponse> items;
    private int limit;
    private int offset;
    private long total;
}
