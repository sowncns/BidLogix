package com.qs.Backend.modules.system.manualhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// Field/property names here follow OnlyOffice's own DocEditor config contract
// (https://api.onlyoffice.com/editors/config/), not erp-fe's snake_case
// convention — this object is handed straight to `new DocsAPI.DocEditor(...)`.
@Getter
@Builder
@AllArgsConstructor
public class OnlyOfficeConfigResponse {
    private String documentType;
    private DocumentInfo document;
    private EditorConfigInfo editorConfig;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DocumentInfo {
        private String fileType;
        private String key;
        private String title;
        private String url;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EditorConfigInfo {
        private String mode;
        private String callbackUrl;
        private String lang;
    }
}
