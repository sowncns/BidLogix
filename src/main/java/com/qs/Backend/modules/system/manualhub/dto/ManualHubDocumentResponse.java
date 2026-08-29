package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// Field names are explicit @JsonProperty (not a naming strategy) because
// Lombok's isXxx()-style boolean getters otherwise get stripped to "xxx" by
// Jackson's default bean-naming, which would silently rename is_current to
// current on the wire — erp-fe's documents.api.ts reads raw.is_current
// verbatim and would just get undefined.
@Getter
@Builder
@AllArgsConstructor
public class ManualHubDocumentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("parent_id")
    private Long parentId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("format")
    private String format;

    @JsonProperty("content")
    private Object content;

    @JsonProperty("status")
    private String status;

    @JsonProperty("language")
    private String language;

    @JsonProperty("version")
    private String version;

    @JsonProperty("author_name")
    private String authorName;

    // Named "current" (not "isCurrent") on purpose: Lombok's getter for a
    // boolean field named "isCurrent" is isCurrent(), whose Jackson-implicit
    // name is "current" — a mismatch against the field's own implicit name
    // that made Jackson serialize it twice (once as "current", once via this
    // @JsonProperty as "is_current"). Naming the field "current" makes both
    // agree, so the annotation is the only name that survives.
    @JsonProperty("is_current")
    private boolean current;

    @JsonProperty("submitted_at")
    private Instant submittedAt;

    @JsonProperty("released_at")
    private Instant releasedAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("file_count")
    private int fileCount;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
}
