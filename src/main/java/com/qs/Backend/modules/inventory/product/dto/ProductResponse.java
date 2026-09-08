package com.qs.Backend.modules.inventory.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private Map<String, Object> specifications;
    @JsonProperty("warranty_months")
    private Integer warrantyMonths;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
    @JsonProperty("deleted_at")
    private Instant deletedAt;
}
