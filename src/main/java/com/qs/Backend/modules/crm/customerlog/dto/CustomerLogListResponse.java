package com.qs.Backend.modules.crm.customerlog.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerLogListResponse {
    private List<CustomerLogResponse> logs;
    private int limit;
    private int offset;
}
