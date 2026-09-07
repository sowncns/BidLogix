package com.qs.Backend.modules.crm.customerstatus.controller;

import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusListResponse;
import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusRequest;
import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusResponse;
import com.qs.Backend.modules.crm.customerstatus.service.CustomerStatusService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer-statuses")
@RequiredArgsConstructor
public class CustomerStatusController {
    private final CustomerStatusService customerStatusService;

    @GetMapping
    public ApiResponse<CustomerStatusListResponse> list(@RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(customerStatusService.list(limit, offset), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerStatusResponse> get(@PathVariable String id) {
        return ApiResponse.ok(customerStatusService.get(id), null);
    }

    @PostMapping
    public ApiResponse<CustomerStatusResponse> create(@RequestBody CustomerStatusRequest request) {
        return ApiResponse.created(customerStatusService.create(request), "Customer status created");
    }

    @PatchMapping("/{id}")
    public ApiResponse<CustomerStatusResponse> update(@PathVariable String id, @RequestBody CustomerStatusRequest request) {
        return ApiResponse.ok(customerStatusService.update(id, request), "Customer status updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        customerStatusService.delete(id);
        return ApiResponse.ok(null, "Customer status deleted");
    }
}
