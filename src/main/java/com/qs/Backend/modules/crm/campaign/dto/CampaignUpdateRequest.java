package com.qs.Backend.modules.crm.campaign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CampaignUpdateRequest {
    private String name;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("business_field_id")
    private String businessFieldId;
    @JsonProperty("assigned_person_id")
    private String assignedPersonId;
    @JsonProperty("channel_type")
    private String channelType;
    @JsonProperty("sla_hours")
    private Integer slaHours;
    @JsonProperty("start_at")
    private Instant startAt;
    @JsonProperty("end_at")
    private Instant endAt;
}
