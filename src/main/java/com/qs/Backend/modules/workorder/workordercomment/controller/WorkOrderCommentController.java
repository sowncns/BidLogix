package com.qs.Backend.modules.workorder.workordercomment.controller;

import com.qs.Backend.modules.workorder.workordercomment.dto.*;
import com.qs.Backend.modules.workorder.workordercomment.service.WorkOrderCommentService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkOrderCommentController {
    private final WorkOrderCommentService workOrderCommentService;

    @GetMapping("/work-orders/{workOrderId}/comments")
    public ApiResponse<WorkOrderCommentListResponse> list(@PathVariable String workOrderId, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(workOrderCommentService.list(workOrderId, limit, offset), null);
    }

    @PostMapping("/work-orders/{workOrderId}/comments")
    public ApiResponse<WorkOrderCommentResponse> create(@PathVariable String workOrderId, @RequestBody WorkOrderCommentCreateRequest request, @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.created(workOrderCommentService.create(workOrderId, request, principal == null ? null : principal.getId()), "Work order comment created");
    }

    @PatchMapping("/work-order-comments/{id}")
    public ApiResponse<WorkOrderCommentResponse> update(@PathVariable String id, @RequestBody WorkOrderCommentUpdateRequest request, @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(workOrderCommentService.update(id, request, principal == null ? null : principal.getId()), "Work order comment updated");
    }

    @DeleteMapping("/work-order-comments/{id}")
    public ApiResponse<Void> delete(@PathVariable String id, @AuthenticationPrincipal AccountPrincipal principal) {
        workOrderCommentService.delete(id, principal == null ? null : principal.getId());
        return ApiResponse.ok(null, "Work order comment deleted");
    }
}
