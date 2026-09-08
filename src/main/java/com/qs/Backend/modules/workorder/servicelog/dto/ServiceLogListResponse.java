package com.qs.Backend.modules.workorder.servicelog.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServiceLogListResponse {
    private List<ServiceLogResponse> items;
    private int limit;
    private int offset;
    private long total;
}
