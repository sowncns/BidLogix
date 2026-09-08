package com.qs.Backend.platform.logging.audit.service;

import com.qs.Backend.platform.logging.audit.dto.AuditLogResponse;
import com.qs.Backend.platform.logging.audit.dto.AuditLogSearchRequest;
import com.qs.Backend.platform.logging.audit.entity.AuditLog;
import com.qs.Backend.platform.logging.audit.repository.AuditLogRepository;
import com.qs.Backend.platform.logging.system.RequestLoggingConstants;
import com.qs.Backend.shared.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(UUID actorId, String actorUsername, String action, String targetType, String targetId, String detail) {
        if (actorId == null) {
            return;
        }
        AuditLog entry = new AuditLog();
        entry.setActorId(actorId);
        entry.setAction(action);
        setTypedTarget(entry, targetType, targetId);
        Map<String, Object> metadata = new LinkedHashMap<>();
        putIfNotNull(metadata, "actor_username", actorUsername);
        putIfNotNull(metadata, "target_type", targetType);
        putIfNotNull(metadata, "target_id", targetId);
        putIfNotNull(metadata, "detail", detail);
        putIfNotNull(metadata, "request_id", MDC.get(RequestLoggingConstants.REQUEST_ID_MDC_KEY));
        putIfNotNull(metadata, "ip_address", currentRequestIp());
        entry.setMetadata(metadata);
        auditLogRepository.save(entry);
    }

    public Page<AuditLogResponse> search(AuditLogSearchRequest filter, Pageable pageable) {
        return auditLogRepository.findAll(toSpecification(filter), pageable)
                .map(AuditLogResponse::from);
    }

    public AuditLogResponse get(UUID id) {
        return auditLogRepository.findById(id)
                .map(AuditLogResponse::from)
                .orElseThrow(() -> new AppException("Audit log not found", HttpStatus.NOT_FOUND, "AUDIT_LOG_NOT_FOUND"));
    }

    private String currentRequestIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Specification<AuditLog> toSpecification(AuditLogSearchRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (filter.actorId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("actorId"), filter.actorId()));
            }
            if (filter.action() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("action"), filter.action()));
            }
            if (filter.from() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), filter.to()));
            }
            return predicates;
        };
    }

    private void setTypedTarget(AuditLog entry, String targetType, String targetId) {
        if (targetType == null || targetId == null) return;
        try {
            UUID id = UUID.fromString(targetId);
            switch (targetType.toLowerCase()) {
                case "authuser", "user" -> entry.setTargetUserId(id);
                case "role" -> entry.setTargetRoleId(id);
                case "permission" -> entry.setTargetPermissionId(id);
                case "organization" -> entry.setTargetOrganizationId(id);
                default -> { }
            }
        } catch (IllegalArgumentException ignored) {
            // Non-UUID business targets remain available in metadata.
        }
    }

    private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) metadata.put(key, value);
    }
}
