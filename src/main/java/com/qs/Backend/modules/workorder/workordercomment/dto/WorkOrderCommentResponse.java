package com.qs.Backend.modules.workorder.workordercomment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WorkOrderCommentResponse {
    private String id;
    @JsonProperty("work_order_id")
    private String workOrderId;
    @JsonProperty("author_id")
    private String authorId;
    @JsonProperty("author_name")
    private String authorName;
    private String content;
    @JsonProperty("is_internal")
    private boolean internal;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
