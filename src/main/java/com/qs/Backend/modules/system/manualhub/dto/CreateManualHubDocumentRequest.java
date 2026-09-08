package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateManualHubDocumentRequest {

    @NotNull
    @JsonProperty("product_id")
    private UUID productId;

    @JsonProperty("parent_id")
    private UUID parentId;

    @JsonProperty("document_group_id")
    private UUID documentGroupId;

    @NotBlank
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("format")
    private String format;

    @JsonProperty("language")
    private String language;

    @JsonProperty("version")
    private String version;

    @JsonProperty("content")
    private Object content;
}
