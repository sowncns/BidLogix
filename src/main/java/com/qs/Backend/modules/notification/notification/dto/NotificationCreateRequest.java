package com.qs.Backend.modules.notification.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationCreateRequest {
    private String userId;
    private String type;
    private Map<String, Object> params;
    private String link;
    private Map<String, Object> data;
}
