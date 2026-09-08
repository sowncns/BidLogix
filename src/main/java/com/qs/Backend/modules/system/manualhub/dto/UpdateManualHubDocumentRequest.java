package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateManualHubDocumentRequest {
    @JsonProperty("product_id")
    private UUID productId;
    @JsonProperty("parent_id")
    private UUID parentId;
    private String title;
    private String description;
    @JsonProperty("document_type")
    private String documentType;
    private String format;
    private String language;
    private String version;
    private Object content;
    private String status;
    @JsonProperty("rejection_reason")
    private String rejectionReason;
}
