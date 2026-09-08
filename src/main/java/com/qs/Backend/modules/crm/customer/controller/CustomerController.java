package com.qs.Backend.modules.crm.customer.controller;

import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerFilterParams;
import com.qs.Backend.modules.crm.customer.dto.CustomerListResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerUpdateRequest;
import com.qs.Backend.modules.crm.customer.service.CustomerService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request,
                                                 @AuthenticationPrincipal AccountPrincipal principal) {
        Long performedBy = principal == null ? null : principal.getId();
        return ApiResponse.created(customerService.createCustomer(request, performedBy), "Customer created");
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> get(@PathVariable String id) {
        return ApiResponse.ok(customerService.getCustomer(id), null);
    }

    @GetMapping
    public ApiResponse<CustomerListResponse> list(
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusId,
            @RequestParam(required = false) String assignedSalesId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtTo,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        CustomerFilterParams filters = new CustomerFilterParams(organizationId, keyword, statusId, assignedSalesId, createdAtFrom, createdAtTo);
        return ApiResponse.ok(customerService.listCustomers(filters, limit, offset), null);
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable String id, @Valid @RequestBody CustomerUpdateRequest request,
                                                 @AuthenticationPrincipal AccountPrincipal principal) {
        Long performedBy = principal == null ? null : principal.getId();
        return ApiResponse.ok(customerService.updateCustomer(id, request, performedBy), "Customer updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable String id, @AuthenticationPrincipal AccountPrincipal principal) {
        Long performedBy = principal == null ? null : principal.getId();
        customerService.deactivateCustomer(id, performedBy);
        return ApiResponse.ok(null, "Customer deactivated");
    }
}
