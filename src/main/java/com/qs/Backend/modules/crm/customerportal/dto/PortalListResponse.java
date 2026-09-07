package com.qs.Backend.modules.crm.customerportal.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortalListResponse<T> {
    private List<T> items;
}
