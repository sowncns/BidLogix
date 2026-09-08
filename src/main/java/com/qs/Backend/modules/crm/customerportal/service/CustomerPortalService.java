package com.qs.Backend.modules.crm.customerportal.service;

import com.qs.Backend.modules.crm.customerportal.dto.PortalListResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestCreateRequest;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalWarrantySummaryResponse;
import com.qs.Backend.modules.workorder.workordercomment.dto.WorkOrderCommentCreateRequest;
import com.qs.Backend.modules.workorder.workordercomment.dto.WorkOrderCommentUpdateRequest;
import com.qs.Backend.modules.workorder.workordercomment.service.WorkOrderCommentService;
import com.qs.Backend.platform.file.service.LinkedFileService;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerPortalService {

    private final LinkedFileService linkedFileService;
    private final WorkOrderCommentService workOrderCommentService;

    public PortalListResponse<Object> productItems() {
        return PortalListResponse.builder().items(List.of()).build();
    }

    public Object productItem(String id) {
        throw new AppException("product item not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public PortalWarrantySummaryResponse warranties() {
        return PortalWarrantySummaryResponse.builder()
                .totalProductItems(0)
                .activeWarranties(0)
                .expiringSoon(0)
                .expired(0)
                .build();
    }

    public PortalListResponse<PortalServiceRequestResponse> serviceRequests() {
        return PortalListResponse.<PortalServiceRequestResponse>builder().items(List.of()).build();
    }

    public PortalServiceRequestResponse serviceRequest(String id) {
        throw new AppException("service request not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public PortalServiceRequestResponse createServiceRequest(PortalServiceRequestCreateRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new AppException("description is required", HttpStatus.BAD_REQUEST, "INVALID_INPUT");
        }
        return PortalServiceRequestResponse.builder()
                .id(UUID.randomUUID().toString())
                .productItemId(request.getProductItemId())
                .type(request.getType())
                .serviceType(request.getServiceType())
                .priority(request.getPriority())
                .description(request.getDescription().trim())
                .status("open")
                .serviceLogs(List.of())
                .attachments(List.of())
                .createdAt(Instant.now())
                .build();
    }

    public Object uploadWorkOrderAttachment(String workOrderId, MultipartFile file) {
        return linkedFileService.upload("work_order", UUID.fromString(workOrderId), "attachment", file, null, 0);
    }

    public ResponseEntity<Resource> downloadWorkOrderAttachment(String workOrderId, String fileId) {
        return download(linkedFileService.download("work_order", UUID.fromString(workOrderId), UUID.fromString(fileId)));
    }

    public Object comments(String workOrderId, int limit, int offset) {
        return workOrderCommentService.list(workOrderId, limit, offset);
    }

    public Object createComment(String workOrderId, WorkOrderCommentCreateRequest request) {
        return workOrderCommentService.create(workOrderId, request, null);
    }

    public Object updateComment(String commentId, WorkOrderCommentUpdateRequest request) {
        return workOrderCommentService.update(commentId, request, null);
    }

    public void deleteComment(String commentId) {
        workOrderCommentService.delete(commentId, null);
    }

    public Object uploadCommentAttachment(String commentId, MultipartFile file) {
        return linkedFileService.upload("work_order_comment", UUID.fromString(commentId), "attachment", file, null, 0);
    }

    public ResponseEntity<Resource> downloadCommentAttachment(String commentId, String fileId) {
        return download(linkedFileService.download("work_order_comment", UUID.fromString(commentId), UUID.fromString(fileId)));
    }

    public void deleteCommentAttachment(String commentId, String fileId) {
        linkedFileService.delete("work_order_comment", UUID.fromString(commentId), UUID.fromString(fileId));
    }

    private ResponseEntity<Resource> download(LinkedFileService.Download download) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.fileName().replace("\"", "") + "\"")
                .body(download.resource());
    }
}
