package com.qs.Backend.modules.notification.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessageResponse(
        DeliveryLogView log,
        String message
) {
}
