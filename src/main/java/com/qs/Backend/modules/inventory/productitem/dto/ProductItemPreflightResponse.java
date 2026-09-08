package com.qs.Backend.modules.inventory.productitem.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductItemPreflightResponse {
    private List<ProductItemPreflightRowResponse> rows;
}
