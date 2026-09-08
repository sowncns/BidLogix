package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ManualHubDocumentVersionResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("document_id")
    private UUID documentId;

    @JsonProperty("version")
    private String version;

    @JsonProperty("title")
    private String title;

    @JsonProperty("format")
    private String format;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("created_by_name")
    private String createdByName;

    @JsonProperty("created_at")
    private Instant createdAt;
}
