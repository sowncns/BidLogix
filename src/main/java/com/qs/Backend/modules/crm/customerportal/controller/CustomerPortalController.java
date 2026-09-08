package com.qs.Backend.modules.crm.customerportal.controller;

import com.qs.Backend.modules.crm.customerportal.dto.PortalListResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestCreateRequest;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalWarrantySummaryResponse;
import com.qs.Backend.modules.crm.customerportal.service.CustomerPortalService;
import com.qs.Backend.modules.workorder.workordercomment.dto.WorkOrderCommentCreateRequest;
import com.qs.Backend.modules.workorder.workordercomment.dto.WorkOrderCommentUpdateRequest;
import com.qs.Backend.shared.response.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CustomerPortalController {
    private final CustomerPortalService customerPortalService;

    @GetMapping("/my/product-items")
    public ApiResponse<PortalListResponse<Object>> productItems() {
        return ApiResponse.ok(customerPortalService.productItems(), null);
    }

    @GetMapping("/my/product-items/{id}")
    public ApiResponse<Object> productItem(@PathVariable String id) {
        return ApiResponse.ok(customerPortalService.productItem(id), null);
    }

    @GetMapping("/my/warranties")
    public ApiResponse<PortalWarrantySummaryResponse> warranties() {
        return ApiResponse.ok(customerPortalService.warranties(), null);
    }

    @GetMapping("/my/service-requests")
    public ApiResponse<PortalListResponse<PortalServiceRequestResponse>> serviceRequests() {
        return ApiResponse.ok(customerPortalService.serviceRequests(), null);
    }

    @GetMapping("/my/service-requests/{id}")
    public ApiResponse<PortalServiceRequestResponse> serviceRequest(@PathVariable String id) {
        return ApiResponse.ok(customerPortalService.serviceRequest(id), null);
    }

    @PostMapping("/my/service-requests")
    public ApiResponse<PortalServiceRequestResponse> createServiceRequest(@RequestBody PortalServiceRequestCreateRequest request) {
        return ApiResponse.created(customerPortalService.createServiceRequest(request), "Service request created");
    }

    @PostMapping("/my/service-requests/{id}/attachments")
    public ApiResponse<?> uploadServiceRequestAttachment(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return ApiResponse.created(customerPortalService.uploadWorkOrderAttachment(id, file), "Attachment uploaded");
    }

    @GetMapping("/my/service-requests/{id}/attachments/{fileId}")
    public ResponseEntity<Resource> downloadServiceRequestAttachment(@PathVariable String id, @PathVariable String fileId) {
        return customerPortalService.downloadWorkOrderAttachment(id, fileId);
    }

    @GetMapping("/my/service-requests/{id}/comments")
    public ApiResponse<?> comments(@PathVariable String id, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(customerPortalService.comments(id, limit, offset), null);
    }

    @PostMapping("/my/service-requests/{id}/comments")
    public ApiResponse<?> createComment(@PathVariable String id, @RequestBody WorkOrderCommentCreateRequest request) {
        return ApiResponse.created(customerPortalService.createComment(id, request), "Comment created");
    }

    @PatchMapping("/my/service-requests/{id}/comments/{commentId}")
    public ApiResponse<?> updateComment(@PathVariable String id, @PathVariable String commentId, @RequestBody WorkOrderCommentUpdateRequest request) {
        return ApiResponse.ok(customerPortalService.updateComment(commentId, request), "Comment updated");
    }

    @DeleteMapping("/my/service-requests/{id}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable String id, @PathVariable String commentId) {
        customerPortalService.deleteComment(commentId);
        return ApiResponse.ok(null, "Comment deleted");
    }

    @PostMapping("/my/service-requests/{id}/comments/{commentId}/attachments")
    public ApiResponse<?> uploadCommentAttachment(@PathVariable String id, @PathVariable String commentId, @RequestParam("file") MultipartFile file) {
        return ApiResponse.created(customerPortalService.uploadCommentAttachment(commentId, file), "Attachment uploaded");
    }

    @GetMapping("/my/service-requests/{id}/comments/{commentId}/attachments/{fileId}")
    public ResponseEntity<Resource> downloadCommentAttachment(@PathVariable String id, @PathVariable String commentId, @PathVariable String fileId) {
        return customerPortalService.downloadCommentAttachment(commentId, fileId);
    }

    @DeleteMapping("/my/service-requests/{id}/comments/{commentId}/attachments/{fileId}")
    public ApiResponse<Void> deleteCommentAttachment(@PathVariable String id, @PathVariable String commentId, @PathVariable String fileId) {
        customerPortalService.deleteCommentAttachment(commentId, fileId);
        return ApiResponse.ok(null, "Attachment deleted");
    }
}
