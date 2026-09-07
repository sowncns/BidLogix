package com.qs.Backend.modules.crm.customerstatus.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerStatusListResponse {
    private List<CustomerStatusResponse> items;
    private int limit;
    private int offset;
}
