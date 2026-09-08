package com.qs.Backend.modules.workorder.workorder.controller;

import com.qs.Backend.modules.workorder.workorder.dto.*;
import com.qs.Backend.modules.workorder.workorder.service.WorkOrderService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    @GetMapping
    public ApiResponse<WorkOrderListResponse> list(@RequestParam(name = "product_item_id", required = false) String productItemId, @RequestParam(name = "assigned_to", required = false) String assignedTo, @RequestParam(required = false) String status, @RequestParam(required = false) String type, @RequestParam(required = false) String priority, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(workOrderService.list(productItemId, assignedTo, status, type, priority, limit, offset), null);
    }

    @PostMapping
    public ApiResponse<WorkOrderResponse> create(@RequestBody WorkOrderCreateRequest request, @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.created(workOrderService.create(request, principal == null ? null : principal.getId()), "Work order created");
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderResponse> get(@PathVariable String id) {
        return ApiResponse.ok(workOrderService.get(id), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<WorkOrderResponse> update(@PathVariable String id, @RequestBody WorkOrderUpdateRequest request) {
        return ApiResponse.ok(workOrderService.update(id, request), "Work order updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        workOrderService.delete(id);
        return ApiResponse.ok(null, "Work order deleted");
    }

    @PostMapping("/{id}/start")
    public ApiResponse<WorkOrderResponse> start(@PathVariable String id) {
        return ApiResponse.ok(workOrderService.start(id), "Work order started");
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<WorkOrderResponse> complete(@PathVariable String id) {
        return ApiResponse.ok(workOrderService.complete(id), "Work order completed");
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<WorkOrderResponse> cancel(@PathVariable String id, @RequestBody(required = false) WorkOrderCancelRequest request) {
        return ApiResponse.ok(workOrderService.cancel(id), "Work order cancelled");
    }

    @PostMapping("/{id}/reopen")
    public ApiResponse<WorkOrderResponse> reopen(@PathVariable String id) {
        return ApiResponse.ok(workOrderService.reopen(id), "Work order reopened");
    }
}
