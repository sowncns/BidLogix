package com.qs.Backend.platform.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class LinkedFileResponse {
    private UUID id;
    @JsonProperty("original_name")
    private String originalName;
    @JsonProperty("mime_type")
    private String mimeType;
    @JsonProperty("size_bytes")
    private long sizeBytes;
    private String visibility;
    @JsonProperty("display_order")
    private int displayOrder;
    @JsonProperty("created_at")
    private Instant createdAt;
}
