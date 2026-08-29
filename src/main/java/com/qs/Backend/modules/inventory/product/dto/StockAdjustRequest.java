package com.qs.Backend.modules.inventory.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustRequest {

    // Positive to increase stock, negative to decrease.
    @NotNull
    private Integer delta;
}
