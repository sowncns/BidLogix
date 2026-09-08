package com.qs.Backend.modules.workorder.workorder.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkOrderListResponse {
    private List<WorkOrderResponse> items;
    private int limit;
    private int offset;
    private long total;
}
