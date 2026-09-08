package com.qs.Backend.modules.workorder.workorder.service;

import com.qs.Backend.modules.workorder.workorder.dto.*;
import com.qs.Backend.modules.workorder.workorder.entity.WorkOrder;
import com.qs.Backend.modules.workorder.workorder.repository.WorkOrderRepository;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private static final List<String> TYPES = List.of("production", "preventive_maintenance", "corrective_maintenance", "customer_request");
    private static final List<String> PRIORITIES = List.of("low", "normal", "high", "urgent");
    private static final List<String> STATUSES = List.of("pending", "in_progress", "completed", "cancelled");
    private final WorkOrderRepository workOrderRepository;

    @Transactional(readOnly = true)
    public WorkOrderListResponse list(String productItemId, String assignedTo, String status, String type, String priority, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = workOrderRepository.findAll(spec(productItemId, assignedTo, status, type, priority), PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return WorkOrderListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).limit(safeLimit).offset(safeOffset).total(page.getTotalElements()).build();
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request, Long createdBy) {
        validateType(request.getType());
        String priority = request.getPriority() == null || request.getPriority().isBlank() ? "normal" : request.getPriority().trim();
        validatePriority(priority);
        if (request.getDescription() == null || request.getDescription().isBlank()) throw invalid("description is required");
        Instant now = Instant.now();
        String datePrefix = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);
        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderNumber("WO-" + datePrefix + "-" + String.format("%04d", workOrderRepository.countByOrderNumberStartingWith("WO-" + datePrefix) + 1));
        workOrder.setProductItemId(blankToNull(request.getProductItemId()));
        workOrder.setType(request.getType().trim());
        workOrder.setPriority(priority);
        workOrder.setAssignedTo(blankToNull(request.getAssignedTo()));
        workOrder.setDescription(request.getDescription().trim());
        workOrder.setScheduledDate(request.getScheduledDate());
        workOrder.setCreatedBy(createdBy == null ? null : String.valueOf(createdBy));
        workOrder.setCreatedAt(now);
        workOrder.setUpdatedAt(now);
        return toResponse(workOrderRepository.save(workOrder));
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public WorkOrderResponse update(String id, WorkOrderUpdateRequest request) {
        WorkOrder workOrder = findOrThrow(id);
        if (request.getType() != null) { validateType(request.getType()); workOrder.setType(request.getType().trim()); }
        if (request.getPriority() != null) { validatePriority(request.getPriority()); workOrder.setPriority(request.getPriority().trim()); }
        if (request.getStatus() != null) { validateStatus(request.getStatus()); workOrder.setStatus(request.getStatus().trim()); }
        if (request.getAssignedTo() != null) workOrder.setAssignedTo(blankToNull(request.getAssignedTo()));
        if (request.getDescription() != null) {
            if (request.getDescription().isBlank()) throw invalid("description is required");
            workOrder.setDescription(request.getDescription().trim());
        }
        if (request.getScheduledDate() != null) workOrder.setScheduledDate(request.getScheduledDate());
        workOrder.setUpdatedAt(Instant.now());
        return toResponse(workOrder);
    }

    @Transactional
    public void delete(String id) {
        WorkOrder workOrder = findOrThrow(id);
        workOrder.setDeletedAt(Instant.now());
        workOrder.setUpdatedAt(Instant.now());
    }

    @Transactional
    public WorkOrderResponse start(String id) {
        WorkOrder workOrder = findOrThrow(id);
        if (!"pending".equals(workOrder.getStatus())) throw conflict("cannot start work order");
        Instant now = Instant.now();
        workOrder.setStatus("in_progress");
        workOrder.setStartedAt(now);
        workOrder.setUpdatedAt(now);
        return toResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse complete(String id) {
        WorkOrder workOrder = findOrThrow(id);
        if (!"in_progress".equals(workOrder.getStatus())) throw conflict("cannot complete work order");
        Instant now = Instant.now();
        workOrder.setStatus("completed");
        workOrder.setCompletedAt(now);
        workOrder.setUpdatedAt(now);
        return toResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse cancel(String id) {
        WorkOrder workOrder = findOrThrow(id);
        if ("completed".equals(workOrder.getStatus()) || "cancelled".equals(workOrder.getStatus())) throw conflict("cannot cancel work order");
        workOrder.setStatus("cancelled");
        workOrder.setUpdatedAt(Instant.now());
        return toResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse reopen(String id) {
        WorkOrder workOrder = findOrThrow(id);
        if (!"completed".equals(workOrder.getStatus()) && !"cancelled".equals(workOrder.getStatus())) throw conflict("cannot reopen work order");
        workOrder.setStatus(workOrder.getAssignedTo() != null && workOrder.getStartedAt() != null ? "in_progress" : "pending");
        workOrder.setCompletedAt(null);
        workOrder.setUpdatedAt(Instant.now());
        return toResponse(workOrder);
    }

    private Specification<WorkOrder> spec(String productItemId, String assignedTo, String status, String type, String priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (productItemId != null && !productItemId.isBlank()) predicates.add(cb.equal(root.get("productItemId"), productItemId.trim()));
            if (assignedTo != null && !assignedTo.isBlank()) predicates.add(cb.equal(root.get("assignedTo"), assignedTo.trim()));
            if (status != null && !status.isBlank()) predicates.add(cb.equal(root.get("status"), status.trim()));
            if (type != null && !type.isBlank()) predicates.add(cb.equal(root.get("type"), type.trim()));
            if (priority != null && !priority.isBlank()) predicates.add(cb.equal(root.get("priority"), priority.trim()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private WorkOrder findOrThrow(String id) {
        return workOrderRepository.findById(id).filter(w -> w.getDeletedAt() == null).orElseThrow(() -> new AppException("Work order not found", HttpStatus.NOT_FOUND, "WORK_ORDER_NOT_FOUND"));
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        return WorkOrderResponse.builder().id(workOrder.getId()).orderNumber(workOrder.getOrderNumber()).productItemId(workOrder.getProductItemId()).type(workOrder.getType()).priority(workOrder.getPriority()).assignedTo(workOrder.getAssignedTo()).description(workOrder.getDescription()).status(workOrder.getStatus()).scheduledDate(workOrder.getScheduledDate()).startedAt(workOrder.getStartedAt()).completedAt(workOrder.getCompletedAt()).createdBy(workOrder.getCreatedBy()).createdAt(workOrder.getCreatedAt()).updatedAt(workOrder.getUpdatedAt()).deletedAt(workOrder.getDeletedAt()).build();
    }

    private void validateType(String type) { if (type == null || !TYPES.contains(type.trim())) throw invalid("invalid work order type"); }
    private void validatePriority(String priority) { if (priority == null || !PRIORITIES.contains(priority.trim())) throw invalid("invalid work order priority"); }
    private void validateStatus(String status) { if (status == null || !STATUSES.contains(status.trim())) throw invalid("invalid work order status"); }
    private AppException invalid(String message) { return new AppException(message, HttpStatus.BAD_REQUEST, "WORK_ORDER_INVALID_REQUEST"); }
    private AppException conflict(String message) { return new AppException(message, HttpStatus.CONFLICT, "WORK_ORDER_INVALID_STATUS"); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
