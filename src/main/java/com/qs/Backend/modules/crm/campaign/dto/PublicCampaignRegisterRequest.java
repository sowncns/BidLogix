package com.qs.Backend.modules.crm.campaign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicCampaignRegisterRequest {
    @JsonProperty("full_name")
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    @JsonProperty("business_field_id")
    private String businessFieldId;
    private String notes;
}
