package com.qs.Backend.platform.logging.audit.dto;

import com.qs.Backend.platform.logging.audit.entity.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
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
                (String) log.getMetadata().get("actor_username"),
                log.getAction(),
                (String) log.getMetadata().get("target_type"),
                (String) log.getMetadata().get("target_id"),
                (String) log.getMetadata().get("detail"),
                (String) log.getMetadata().get("ip_address"),
                (String) log.getMetadata().get("request_id"),
                log.getCreatedAt()
        );
    }
}
