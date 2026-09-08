package com.qs.Backend.platform.logging.audit.service;

import com.qs.Backend.platform.logging.audit.dto.AuditLogResponse;
import com.qs.Backend.platform.logging.audit.dto.AuditLogSearchRequest;
import com.qs.Backend.platform.logging.audit.entity.AuditLog;
import com.qs.Backend.platform.logging.audit.repository.AuditLogRepository;
import com.qs.Backend.platform.logging.system.RequestLoggingConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long actorId, String actorUsername, String action, String targetType, String targetId, String detail) {
        AuditLog entry = new AuditLog();
        entry.setActorId(actorId);
        entry.setActorUsername(actorUsername);
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetail(detail);
        entry.setRequestId(MDC.get(RequestLoggingConstants.REQUEST_ID_MDC_KEY));
        entry.setIpAddress(currentRequestIp());
        auditLogRepository.save(entry);
    }

    public Page<AuditLogResponse> search(AuditLogSearchRequest filter, Pageable pageable) {
        return auditLogRepository.findAll(toSpecification(filter), pageable)
                .map(AuditLogResponse::from);
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
            if (filter.targetType() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetType"), filter.targetType()));
            }
            if (filter.targetId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetId"), filter.targetId()));
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
}
