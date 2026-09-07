package com.qs.Backend.modules.crm.campaign.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CampaignListResponse {
    private List<CampaignResponse> items;
    private long total;
    private int limit;
    private int offset;
}
