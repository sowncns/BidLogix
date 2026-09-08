package com.qs.Backend.modules.inventory.productitem.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemBulkActivateResponse {

    private int total;
    private int success;
    private int failed;
    private List<ProductItemBulkRowResponse> rows;
}
