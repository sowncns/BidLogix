package com.qs.Backend.modules.workorder.workordercomment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkOrderCommentListResponse {
    private List<WorkOrderCommentResponse> items;
    private int limit;
    private int offset;
    private long total;
}
