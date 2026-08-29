package com.qs.Backend.modules.system.manualhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// erp-fe reads this one as plain camelCase (ManualHubStats in types.ts), no
// snake_case mapping — unlike ManualHubDocumentResponse.
@Getter
@Builder
@AllArgsConstructor
public class ManualHubStatsResponse {
    private long released;
    private long drafts;
    private long waitingReview;
    private long rejected;
}
