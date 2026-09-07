package com.qs.Backend.modules.crm.campaign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignResponse {
    private String id;
    private String name;
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
    private String funnelType;
    @JsonProperty("channel_type")
    private String channelType;
    private String status;
    @JsonProperty("sla_hours")
    private Integer slaHours;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("start_at")
    private Instant startAt;
    @JsonProperty("end_at")
    private Instant endAt;
    @JsonProperty("created_at")
    private Instant createdAt;
    @JsonProperty("updated_at")
    private Instant updatedAt;
}
