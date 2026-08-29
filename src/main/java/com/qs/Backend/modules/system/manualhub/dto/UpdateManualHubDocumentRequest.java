package com.qs.Backend.modules.system.manualhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateManualHubDocumentRequest extends CreateManualHubDocumentRequest {

    @JsonProperty("status")
    private String status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;
}
