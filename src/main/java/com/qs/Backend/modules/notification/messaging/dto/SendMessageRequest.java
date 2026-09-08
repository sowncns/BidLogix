package com.qs.Backend.modules.notification.messaging.dto;

import java.util.Map;

public record SendMessageRequest(
        String channel,
        String recipient,
        String template,
        Map<String, String> params
) {
}
