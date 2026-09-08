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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request,
                                                 @AuthenticationPrincipal AccountPrincipal principal) {
        UUID performedBy = principal == null ? null : principal.getId();
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

    @GetMapping("/tracking-stats")
    public ApiResponse<?> trackingStats(@RequestParam(name = "organization_id", required = false) String organizationId,
                                        @AuthenticationPrincipal AccountPrincipal principal) {
        return ApiResponse.ok(customerService.trackingStats(organizationId, principal == null ? null : principal.getId()), null);
    }

    @GetMapping("/export-template")
    public org.springframework.http.ResponseEntity<String> exportTemplate() {
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"customers-template.csv\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body("full_name,email,phone,company_name,source,status_id,assigned_sales_id,main_phone,main_email,website,address,gender,region\n");
    }

    @PostMapping("/import")
    public ApiResponse<?> importCustomers(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ApiResponse.ok(Map.of("imported", 0, "skipped", 0, "file_name", file.getOriginalFilename()), "Import endpoint available; parser parity pending");
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ApiResponse<CustomerResponse> update(@PathVariable String id, @Valid @RequestBody CustomerUpdateRequest request,
                                                  @AuthenticationPrincipal AccountPrincipal principal) {
        UUID performedBy = principal == null ? null : principal.getId();
        return ApiResponse.ok(customerService.updateCustomer(id, request, performedBy), "Customer updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable String id, @AuthenticationPrincipal AccountPrincipal principal) {
        UUID performedBy = principal == null ? null : principal.getId();
        customerService.deactivateCustomer(id, performedBy);
        return ApiResponse.ok(null, "Customer deactivated");
    }

    @PostMapping("/{id}/grant-portal")
    public ApiResponse<?> grantPortal(@PathVariable String id) {
        return ApiResponse.ok(customerService.grantPortal(id), "Portal access granted");
    }

    @GetMapping("/{id}/portal")
    public ApiResponse<?> portalStatus(@PathVariable String id) {
        return ApiResponse.ok(customerService.portalStatus(id), null);
    }

    @PostMapping("/{id}/revoke-portal")
    public ApiResponse<?> revokePortal(@PathVariable String id) {
        return ApiResponse.ok(customerService.revokePortal(id), "Portal access revoked");
    }

    @PostMapping("/{id}/reset-portal-password")
    public ApiResponse<?> resetPortalPassword(@PathVariable String id) {
        return ApiResponse.ok(customerService.resetPortalPassword(id), "Portal password reset sent");
    }

    @PostMapping("/{id}/merge")
    public ApiResponse<CustomerResponse> merge(@PathVariable String id,
                                               @RequestBody Map<String, String> request,
                                               @AuthenticationPrincipal AccountPrincipal principal) {
        String sourceId = request.getOrDefault("source_id", request.get("sourceId"));
        return ApiResponse.ok(customerService.merge(id, sourceId, principal == null ? null : principal.getId()), "Customer merged");
    }
}
