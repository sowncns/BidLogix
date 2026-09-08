package com.qs.Backend.modules.workorder.workordercomment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderCommentCreateRequest {
    private String content;
    @JsonProperty("is_internal")
    private boolean internal;
}
