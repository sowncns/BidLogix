package com.qs.Backend.modules.crm.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomerListResponse {
    private List<CustomerResponse> items;
    private int limit;
    private int offset;
    private long total;
}
