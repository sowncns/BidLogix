package com.qs.Backend.modules.workorder.workorder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderCancelRequest {
    private String reason;
}
