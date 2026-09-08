package com.qs.Backend.modules.inventory.keygen.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class KeyGenHistoryListResponse {
    private List<KeyGenHistoryResponse> items;
    private long total;
    private int limit;
    private int offset;
}
