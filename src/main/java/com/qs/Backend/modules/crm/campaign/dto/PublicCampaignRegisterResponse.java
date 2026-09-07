package com.qs.Backend.modules.crm.campaign.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicCampaignRegisterResponse {
    private String type;
    private String id;
}
