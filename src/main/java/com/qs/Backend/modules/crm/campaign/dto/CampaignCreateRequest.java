package com.qs.Backend.modules.crm.campaign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CampaignCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String slug;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("business_field_id")
    private String businessFieldId;
    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("assigned_person_id")
    private String assignedPersonId;
    @JsonProperty("funnel_type")
    @NotBlank
    private String funnelType;
    @JsonProperty("channel_type")
    private String channelType;
    @JsonProperty("sla_hours")
    private Integer slaHours;
    @JsonProperty("start_at")
    private Instant startAt;
    @JsonProperty("end_at")
    private Instant endAt;
}
