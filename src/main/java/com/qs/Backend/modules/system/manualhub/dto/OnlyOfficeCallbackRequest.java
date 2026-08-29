package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// https://api.onlyoffice.com/editors/callback — only the fields this backend
// actually reads; everything else (users, actions, ...) is ignored.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlyOfficeCallbackRequest {
    private Integer status;
    private String url;
    private String key;
}
