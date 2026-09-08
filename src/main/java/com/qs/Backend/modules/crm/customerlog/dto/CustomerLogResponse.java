package com.qs.Backend.modules.crm.customerlog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class CustomerLogResponse {
    private String id;
    @JsonProperty("actor_id")
    private String actorId;
    private String action;
    @JsonProperty("target_type")
    private String targetType;
    @JsonProperty("target_id")
    private String targetId;
    private List<FieldChangeResponse> changes;
    @JsonProperty("created_at")
    private Instant createdAt;

    @Getter
    @Builder
    public static class FieldChangeResponse {
        private String field;
        private Object old;
        @JsonProperty("new")
        private Object newValue;
    }
}
