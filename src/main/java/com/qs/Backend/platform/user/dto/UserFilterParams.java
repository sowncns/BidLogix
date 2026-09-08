package com.qs.Backend.platform.user.dto;

import java.time.Instant;

public record UserFilterParams(
        String keyword,
        Instant createdAtFrom,
        Instant createdAtTo,
        String roleCode,
        String permissionCode
) {
}
