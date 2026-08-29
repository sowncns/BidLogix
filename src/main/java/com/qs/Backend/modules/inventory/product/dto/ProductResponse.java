package com.qs.Backend.modules.inventory.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
    private Instant createdAt;
}
