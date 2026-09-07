package com.qs.Backend.modules.crm.activity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityResponse {
    private String id;
    @JsonProperty("activityable_type")
    private String activityableType;
    @JsonProperty("activityable_id")
    private String activityableId;
    @JsonProperty("sales_id")
    private String salesId;
    @JsonProperty("sales_name")
    private String salesName;
    private String action;
    private String content;
    @JsonProperty("contact_at")
    private Instant contactAt;
    @JsonProperty("created_at")
    private Instant createdAt;
    private List<ImageResponse> images;

    @Getter
    @Builder
    public static class ImageResponse {
        private String id;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("display_order")
        private int displayOrder;
        @JsonProperty("created_at")
        private Instant createdAt;
    }
}
