package com.qs.Backend.modules.crm.lead.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LeadListResponse {
    private List<LeadResponse> items;
    private long total;
    private int limit;
    private int offset;
}
