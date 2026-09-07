package com.qs.Backend.modules.crm.customerportal.controller;

import com.qs.Backend.modules.crm.customerportal.dto.PortalListResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestCreateRequest;
import com.qs.Backend.modules.crm.customerportal.dto.PortalServiceRequestResponse;
import com.qs.Backend.modules.crm.customerportal.dto.PortalWarrantySummaryResponse;
import com.qs.Backend.modules.crm.customerportal.service.CustomerPortalService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
