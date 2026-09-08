package com.qs.Backend.modules.inventory.keygen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class KeyGenHistoryResponse {
    private Long id;
    @JsonProperty("key_type")
    private String keyType;
    @JsonProperty("input_data")
    private Map<String, Object> inputData;
    @JsonProperty("output_data")
    private Map<String, Object> outputData;
    @JsonProperty("generated_by")
    private Long generatedBy;
    @JsonProperty("generated_by_name")
    private String generatedByName;
    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("product_item_id")
    private Long productItemId;
    @JsonProperty("product_item_code")
    private String productItemCode;
    @JsonProperty("created_at")
    private Instant createdAt;
}
