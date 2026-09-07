package com.qs.Backend.modules.crm.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    @JsonProperty("total_customers")
    private long totalCustomers;
    @JsonProperty("total_users")
    private long totalUsers;
}
