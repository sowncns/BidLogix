package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UploadFileResponse {

    @JsonProperty("file")
    private FileInfo file;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FileInfo {
        @JsonProperty("url")
        private String url;
    }
}
