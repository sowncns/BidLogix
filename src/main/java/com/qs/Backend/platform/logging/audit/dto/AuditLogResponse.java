package com.qs.Backend.platform.logging.audit.dto;

import com.qs.Backend.platform.logging.audit.entity.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        Long id,
        UUID actorId,
        String actorUsername,
        String action,
        String targetType,
        String targetId,
        String detail,
        String ipAddress,
        String requestId,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorId(),
                log.getActorUsername(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetail(),
                log.getIpAddress(),
                log.getRequestId(),
                log.getCreatedAt()
        );
    }
}
