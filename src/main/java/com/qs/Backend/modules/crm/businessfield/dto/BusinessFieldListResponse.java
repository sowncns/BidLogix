package com.qs.Backend.modules.crm.businessfield.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BusinessFieldListResponse {
    private List<BusinessFieldResponse> items;
    private int limit;
    private int offset;
}
