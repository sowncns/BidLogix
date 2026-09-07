package com.qs.Backend.modules.crm.campaign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicCampaignResponse {
    private String name;
    private String slug;
    @JsonProperty("funnel_type")
    private String funnelType;
    private String status;
}
