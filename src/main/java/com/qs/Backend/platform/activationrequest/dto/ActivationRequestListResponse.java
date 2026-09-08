package com.qs.Backend.platform.activationrequest.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActivationRequestListResponse {
    private List<ActivationRequestResponse> items;
    private long total;
    private int limit;
    private int offset;
}
