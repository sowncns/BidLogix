package com.qs.Backend.modules.sales.salesorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesOrderCreateRequest {

    @NotNull
    private Long customerId;

    @NotNull
    @Positive
    private BigDecimal totalAmount;
}
