package com.qs.Backend.modules.notification.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class NotificationResponse {
    private String id;
    private String type;
    private Map<String, Object> params;
    private String link;
    private Map<String, Object> data;
    @JsonProperty("read_at")
    private Instant readAt;
    @JsonProperty("created_at")
    private Instant createdAt;
}
