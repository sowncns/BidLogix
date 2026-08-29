package com.qs.Backend.modules.crm.customer.controller;

import com.qs.Backend.modules.crm.customer.dto.CustomerCreateRequest;
import com.qs.Backend.modules.crm.customer.dto.CustomerResponse;
import com.qs.Backend.modules.crm.customer.dto.CustomerUpdateRequest;
import com.qs.Backend.modules.crm.customer.service.CustomerService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ApiResponse.created(customerService.createCustomer(request), "Customer created");
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getCustomer(id), null);
    }

    @GetMapping
    public ApiResponse<Page<CustomerResponse>> list(Pageable pageable) {
        return ApiResponse.ok(customerService.listCustomers(pageable), null);
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        return ApiResponse.ok(customerService.updateCustomer(id, request), "Customer updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        customerService.deactivateCustomer(id);
        return ApiResponse.ok(null, "Customer deactivated");
    }
}
