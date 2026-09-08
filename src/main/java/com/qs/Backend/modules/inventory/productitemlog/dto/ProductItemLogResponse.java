package com.qs.Backend.modules.inventory.productitemlog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ProductItemLogResponse {

    private UUID id;

    @JsonProperty("product_item_id")
    private UUID productItemId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("actor_id")
    private UUID actorId;

    @JsonProperty("actor_role")
    private String actorRole;

    private String source;

    @JsonProperty("customer_id")
    private UUID customerId;

    private Map<String, Object> metadata;

    @JsonProperty("occurred_at")
    private Instant occurredAt;

    @JsonProperty("created_at")
    private Instant createdAt;
}
