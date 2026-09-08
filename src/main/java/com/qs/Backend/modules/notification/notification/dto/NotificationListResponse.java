package com.qs.Backend.modules.notification.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResponse {
    private List<NotificationResponse> items;
    @JsonProperty("next_cursor")
    private String nextCursor;
}
