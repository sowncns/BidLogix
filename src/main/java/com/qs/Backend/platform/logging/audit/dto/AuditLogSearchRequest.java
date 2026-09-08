package com.qs.Backend.platform.logging.audit.dto;

import java.time.Instant;
import java.util.UUID;

// All fields optional - null means "don't filter on this".
public record AuditLogSearchRequest(
        UUID actorId,
        String action,
        String targetType,
        String targetId,
        Instant from,
        Instant to
) {
}
