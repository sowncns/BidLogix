package com.qs.Backend.modules.workorder.servicelog.controller;

import com.qs.Backend.modules.workorder.servicelog.dto.*;
import com.qs.Backend.modules.workorder.servicelog.service.ServiceLogService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service-logs")
@RequiredArgsConstructor
public class ServiceLogController {
    private final ServiceLogService serviceLogService;

    @GetMapping
    public ApiResponse<ServiceLogListResponse> list(@RequestParam(name = "product_item_id", required = false) String productItemId, @RequestParam(name = "work_order_id", required = false) String workOrderId, @RequestParam(name = "technician_id", required = false) String technicianId, @RequestParam(name = "service_type", required = false) String serviceType, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(serviceLogService.list(productItemId, workOrderId, technicianId, serviceType, limit, offset), null);
    }

    @PostMapping
    public ApiResponse<ServiceLogResponse> create(@RequestBody ServiceLogCreateRequest request) {
        return ApiResponse.created(serviceLogService.create(request), "Service log created");
    }

    @GetMapping("/{id}")
    public ApiResponse<ServiceLogResponse> get(@PathVariable String id) {
        return ApiResponse.ok(serviceLogService.get(id), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ServiceLogResponse> update(@PathVariable String id, @RequestBody ServiceLogUpdateRequest request) {
        return ApiResponse.ok(serviceLogService.update(id, request), "Service log updated");
    }
}
