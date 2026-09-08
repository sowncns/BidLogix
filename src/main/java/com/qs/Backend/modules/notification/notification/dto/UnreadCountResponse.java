package com.qs.Backend.modules.notification.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {
    private long count;
}
