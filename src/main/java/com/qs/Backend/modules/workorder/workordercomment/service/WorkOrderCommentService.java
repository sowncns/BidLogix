package com.qs.Backend.modules.workorder.workordercomment.service;

import com.qs.Backend.modules.workorder.workorder.repository.WorkOrderRepository;
import com.qs.Backend.modules.workorder.workordercomment.dto.*;
import com.qs.Backend.modules.workorder.workordercomment.entity.WorkOrderComment;
import com.qs.Backend.modules.workorder.workordercomment.repository.WorkOrderCommentRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkOrderCommentService {
    private static final int MAX_CONTENT_LENGTH = 5000;
    private final WorkOrderCommentRepository workOrderCommentRepository;
    private final WorkOrderRepository workOrderRepository;

    @Transactional(readOnly = true)
    public WorkOrderCommentListResponse list(String workOrderId, int limit, int offset) {
        ensureWorkOrderExists(workOrderId);
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = workOrderCommentRepository.findByWorkOrderIdAndDeletedAtIsNull(workOrderId, PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by(Sort.Direction.ASC, "createdAt")));
        return WorkOrderCommentListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).limit(safeLimit).offset(safeOffset).total(page.getTotalElements()).build();
    }

    @Transactional
    public WorkOrderCommentResponse create(String workOrderId, WorkOrderCommentCreateRequest request, Long authorId) {
        ensureWorkOrderExists(workOrderId);
        if (authorId == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        WorkOrderComment comment = new WorkOrderComment();
        comment.setWorkOrderId(workOrderId);
        comment.setAuthorId(String.valueOf(authorId));
        comment.setContent(normalizeContent(request.getContent()));
        comment.setInternal(request.isInternal());
        Instant now = Instant.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        return toResponse(workOrderCommentRepository.save(comment));
    }

    @Transactional
    public WorkOrderCommentResponse update(String id, WorkOrderCommentUpdateRequest request, Long actorId) {
        WorkOrderComment comment = findOrThrow(id);
        ensureAuthor(comment, actorId);
        comment.setContent(normalizeContent(request.getContent()));
        comment.setUpdatedAt(Instant.now());
        return toResponse(comment);
    }

    @Transactional
    public void delete(String id, Long actorId) {
        WorkOrderComment comment = findOrThrow(id);
        ensureAuthor(comment, actorId);
        comment.setDeletedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
    }

    private void ensureWorkOrderExists(String workOrderId) {
        if (workOrderId == null || workOrderId.isBlank() || workOrderRepository.findById(workOrderId).filter(w -> w.getDeletedAt() == null).isEmpty()) {
            throw new AppException("Work order not found", HttpStatus.NOT_FOUND, "WORK_ORDER_NOT_FOUND");
        }
    }

    private WorkOrderComment findOrThrow(String id) {
        return workOrderCommentRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new AppException("Work order comment not found", HttpStatus.NOT_FOUND, "WORK_ORDER_COMMENT_NOT_FOUND"));
    }

    private void ensureAuthor(WorkOrderComment comment, Long actorId) {
        if (actorId == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        if (!comment.getAuthorId().equals(String.valueOf(actorId))) throw new AppException("Forbidden", HttpStatus.FORBIDDEN, "WORK_ORDER_COMMENT_FORBIDDEN");
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) throw new AppException("content is required", HttpStatus.BAD_REQUEST, "WORK_ORDER_COMMENT_INVALID_CONTENT");
        String normalized = content.trim();
        if (normalized.length() > MAX_CONTENT_LENGTH) throw new AppException("content is too long", HttpStatus.BAD_REQUEST, "WORK_ORDER_COMMENT_INVALID_CONTENT");
        return normalized;
    }

    private WorkOrderCommentResponse toResponse(WorkOrderComment comment) {
        return WorkOrderCommentResponse.builder().id(comment.getId()).workOrderId(comment.getWorkOrderId()).authorId(comment.getAuthorId()).content(comment.getContent()).internal(comment.isInternal()).createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt()).build();
    }
}
