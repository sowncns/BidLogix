package com.qs.Backend.modules.crm.customergroup.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerGroupListResponse {
    private List<CustomerGroupResponse> items;
    private int limit;
    private int offset;
}
