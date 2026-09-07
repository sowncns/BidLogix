package com.qs.Backend.modules.crm.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActivityListResponse {
    private List<ActivityResponse> items;
    private int limit;
    private int offset;
}
