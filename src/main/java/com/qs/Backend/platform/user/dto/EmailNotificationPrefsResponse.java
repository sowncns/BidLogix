package com.qs.Backend.platform.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailNotificationPrefsResponse(
        @JsonProperty("activation_request") boolean activationRequest,
        @JsonProperty("service_request") boolean serviceRequest
) {
}
