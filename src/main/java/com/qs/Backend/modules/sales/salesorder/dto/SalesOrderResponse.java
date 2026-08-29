package com.qs.Backend.modules.sales.salesorder.dto;

import com.qs.Backend.modules.sales.salesorder.entity.SalesOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class SalesOrderResponse {
    private Long id;
    private String code;
    private Long customerId;
    private BigDecimal totalAmount;
    private SalesOrderStatus status;
    private Instant createdAt;
}
