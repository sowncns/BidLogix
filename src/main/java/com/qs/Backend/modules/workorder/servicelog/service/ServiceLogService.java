package com.qs.Backend.modules.workorder.servicelog.service;

import com.qs.Backend.modules.workorder.servicelog.dto.*;
import com.qs.Backend.modules.workorder.servicelog.entity.ServiceLog;
import com.qs.Backend.modules.workorder.servicelog.repository.ServiceLogRepository;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceLogService {
    private static final List<String> SERVICE_TYPES = List.of("installation", "maintenance", "repair", "inspection", "note", "event");
    private final ServiceLogRepository serviceLogRepository;

    @Transactional(readOnly = true)
    public ServiceLogListResponse list(String productItemId, String workOrderId, String technicianId, String serviceType, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = serviceLogRepository.findAll(spec(productItemId, workOrderId, technicianId, serviceType), PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "serviceDate")));
        return ServiceLogListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).limit(safeLimit).offset(safeOffset).total(page.getTotalElements()).build();
    }

    @Transactional
    public ServiceLogResponse create(ServiceLogCreateRequest request) {
        if (request.getServiceType() == null || !SERVICE_TYPES.contains(request.getServiceType().trim()) || request.getServiceType().equals("note") || request.getServiceType().equals("event")) throw invalid("invalid service_type");
        if (request.getActionTaken() == null || request.getActionTaken().isBlank()) throw invalid("action_taken is required");
        ServiceLog log = new ServiceLog();
        apply(request, log, true);
        Instant now = Instant.now();
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        return toResponse(serviceLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public ServiceLogResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ServiceLogResponse update(String id, ServiceLogUpdateRequest request) {
        ServiceLog log = findOrThrow(id);
        if (log.getSourceCommentId() != null || log.getEventType() != null) throw new AppException("Service log entry is not editable", HttpStatus.CONFLICT, "SERVICE_LOG_NOT_EDITABLE");
        apply(request, log, false);
        log.setUpdatedAt(Instant.now());
        return toResponse(log);
    }

    private void apply(ServiceLogCreateRequest request, ServiceLog log, boolean creating) {
        if (creating || request.getProductItemId() != null) log.setProductItemId(blankToNull(request.getProductItemId()));
        if (request.getWorkOrderId() != null) log.setWorkOrderId(blankToNull(request.getWorkOrderId()));
        if (request.getServiceDate() != null) log.setServiceDate(request.getServiceDate());
        if (request.getServiceType() != null) {
            if (!SERVICE_TYPES.contains(request.getServiceType().trim()) || request.getServiceType().equals("note") || request.getServiceType().equals("event")) throw invalid("invalid service_type");
            log.setServiceType(request.getServiceType().trim());
        }
        if (request.getIssueDescription() != null) log.setIssueDescription(request.getIssueDescription().trim());
        if (request.getActionTaken() != null) {
            if (request.getActionTaken().isBlank()) throw invalid("action_taken is required");
            log.setActionTaken(request.getActionTaken().trim());
        }
        if (request.getPartsReplaced() != null) log.setPartsReplaced(request.getPartsReplaced().stream().map(this::toEntityPart).toList());
        if (request.getTechnicianId() != null) log.setTechnicianId(blankToNull(request.getTechnicianId()));
        if (request.getCustomerNotes() != null) log.setCustomerNotes(request.getCustomerNotes().trim());
        if (request.getInternalNotes() != null) log.setInternalNotes(request.getInternalNotes().trim());
    }

    private Specification<ServiceLog> spec(String productItemId, String workOrderId, String technicianId, String serviceType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productItemId != null && !productItemId.isBlank()) predicates.add(cb.equal(root.get("productItemId"), productItemId.trim()));
            if (workOrderId != null && !workOrderId.isBlank()) predicates.add(cb.equal(root.get("workOrderId"), workOrderId.trim()));
            if (technicianId != null && !technicianId.isBlank()) predicates.add(cb.equal(root.get("technicianId"), technicianId.trim()));
            if (serviceType != null && !serviceType.isBlank()) predicates.add(cb.equal(root.get("serviceType"), serviceType.trim()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ServiceLog findOrThrow(String id) {
        return serviceLogRepository.findById(id).orElseThrow(() -> new AppException("Service log not found", HttpStatus.NOT_FOUND, "SERVICE_LOG_NOT_FOUND"));
    }

    private ServiceLogResponse toResponse(ServiceLog log) {
        return ServiceLogResponse.builder().id(log.getId()).productItemId(log.getProductItemId()).workOrderId(log.getWorkOrderId()).serviceDate(log.getServiceDate()).serviceType(log.getServiceType()).issueDescription(log.getIssueDescription()).actionTaken(log.getActionTaken()).partsReplaced(log.getPartsReplaced().stream().map(this::toDtoPart).toList()).technicianId(log.getTechnicianId()).customerNotes(log.getCustomerNotes()).internalNotes(log.getInternalNotes()).sourceCommentId(log.getSourceCommentId()).eventType(log.getEventType()).eventMetadata(log.getEventMetadata()).createdAt(log.getCreatedAt()).updatedAt(log.getUpdatedAt()).build();
    }

    private ServiceLog.PartReplaced toEntityPart(PartReplacedDto dto) {
        ServiceLog.PartReplaced part = new ServiceLog.PartReplaced();
        part.setPartName(dto.getPartName());
        part.setQuantity(dto.getQuantity());
        part.setPartCode(dto.getPartCode());
        return part;
    }

    private PartReplacedDto toDtoPart(ServiceLog.PartReplaced part) {
        PartReplacedDto dto = new PartReplacedDto();
        dto.setPartName(part.getPartName());
        dto.setQuantity(part.getQuantity());
        dto.setPartCode(part.getPartCode());
        return dto;
    }

    private AppException invalid(String message) { return new AppException(message, HttpStatus.BAD_REQUEST, "SERVICE_LOG_INVALID_REQUEST"); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
