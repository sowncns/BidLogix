package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ManualHubDocumentVersionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("version")
    private String version;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("created_by_name")
    private String createdByName;

    @JsonProperty("created_at")
    private Instant createdAt;
}
