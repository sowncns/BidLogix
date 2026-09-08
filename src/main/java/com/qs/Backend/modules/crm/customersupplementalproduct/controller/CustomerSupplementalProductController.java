package com.qs.Backend.modules.crm.customersupplementalproduct.controller;

import com.qs.Backend.modules.crm.customersupplementalproduct.dto.*;
import com.qs.Backend.modules.crm.customersupplementalproduct.service.CustomerSupplementalProductService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CustomerSupplementalProductController {
    private final CustomerSupplementalProductService customerSupplementalProductService;

    @GetMapping("/customers/{id}/supplemental-products")
    public ApiResponse<CustomerSupplementalProductListResponse> list(@PathVariable String id) {
        return ApiResponse.ok(customerSupplementalProductService.listByCustomer(id), null);
    }

    @PostMapping("/customers/{id}/supplemental-products")
    public ApiResponse<CustomerSupplementalProductResponse> create(@PathVariable String id, @RequestBody CustomerSupplementalProductCreateRequest request, @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.created(customerSupplementalProductService.create(id, request, principal == null ? null : principal.getId()), "Supplemental product created");
    }
}
