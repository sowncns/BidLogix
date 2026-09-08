package com.qs.Backend.modules.notification.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryLogView(
        String id,
        @JsonProperty("user_id") String userId,
        String channel,
        String provider,
        String recipient,
        String template,
        String status,
        String error,
        @JsonProperty("provider_message_id") String providerMessageId,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("sent_at") Instant sentAt
) {
}
