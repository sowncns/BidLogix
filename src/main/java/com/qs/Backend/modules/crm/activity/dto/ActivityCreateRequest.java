package com.qs.Backend.modules.crm.activity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ActivityCreateRequest {
    @JsonProperty("sales_id")
    private String salesId;
    private String action;
    private String content;
    @JsonProperty("contact_at")
    private Instant contactAt;
}
