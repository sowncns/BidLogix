package com.qs.Backend.modules.crm.customergroup.controller;

import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupListResponse;
import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupRequest;
import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupResponse;
import com.qs.Backend.modules.crm.customergroup.service.CustomerGroupService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer-groups")
@RequiredArgsConstructor
public class CustomerGroupController {
    private final CustomerGroupService customerGroupService;

    @GetMapping
    public ApiResponse<CustomerGroupListResponse> list(@RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(customerGroupService.list(limit, offset), null);
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerGroupResponse> get(@PathVariable String id) {
        return ApiResponse.ok(customerGroupService.get(id), null);
    }

    @PostMapping
    public ApiResponse<CustomerGroupResponse> create(@RequestBody CustomerGroupRequest request) {
        return ApiResponse.created(customerGroupService.create(request), "Customer group created");
    }

    @PatchMapping("/{id}")
    public ApiResponse<CustomerGroupResponse> update(@PathVariable String id, @RequestBody CustomerGroupRequest request) {
        return ApiResponse.ok(customerGroupService.update(id, request), "Customer group updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        customerGroupService.delete(id);
        return ApiResponse.ok(null, "Customer group deleted");
    }
}
