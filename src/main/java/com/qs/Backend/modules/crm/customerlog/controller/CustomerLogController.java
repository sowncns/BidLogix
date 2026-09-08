package com.qs.Backend.modules.crm.customerlog.controller;

import com.qs.Backend.modules.crm.customerlog.dto.CustomerLogListResponse;
import com.qs.Backend.modules.crm.customerlog.service.CustomerLogService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerLogController {
    private final CustomerLogService customerLogService;

    @GetMapping("/customers/{id}/logs")
    public ApiResponse<CustomerLogListResponse> list(@PathVariable String id, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(customerLogService.list(id, limit, offset), null);
    }
}
