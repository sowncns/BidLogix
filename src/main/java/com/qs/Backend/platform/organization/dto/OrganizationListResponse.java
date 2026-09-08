package com.qs.Backend.platform.organization.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrganizationListResponse {
    private List<OrganizationResponse> items;
    private int limit;
    private int offset;
}
