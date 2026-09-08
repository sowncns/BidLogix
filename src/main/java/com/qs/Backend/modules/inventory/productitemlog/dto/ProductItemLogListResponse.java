package com.qs.Backend.modules.inventory.productitemlog.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductItemLogListResponse {

    private List<ProductItemLogResponse> items;
    private int limit;
    private int offset;
    private long total;
}
