package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ManualHubActivityResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("document_title")
    private String documentTitle;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("actor_id")
    private Long actorId;

    @JsonProperty("actor_name")
    private String actorName;

    @JsonProperty("action")
    private String action;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private Instant createdAt;
}
