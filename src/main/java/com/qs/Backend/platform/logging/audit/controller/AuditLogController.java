package com.qs.Backend.platform.logging.audit.controller;

import com.qs.Backend.platform.logging.audit.dto.AuditLogResponse;
import com.qs.Backend.platform.logging.audit.dto.AuditLogSearchRequest;
import com.qs.Backend.platform.logging.audit.service.AuditLogService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW')")
    public ApiResponse<Page<AuditLogResponse>> search(AuditLogSearchRequest filter, Pageable pageable) {
        return ApiResponse.ok(auditLogService.search(filter, pageable), null);
    }
}
