package com.qs.Backend.modules.crm.customer.dto;

import java.time.Instant;

public record CustomerFilterParams(
        String organizationId,
        String keyword,
        String statusId,
        String assignedSalesId,
        Instant createdAtFrom,
        Instant createdAtTo
) {
}
