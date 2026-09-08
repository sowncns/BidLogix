package com.qs.Backend.platform.organization.controller;

import com.qs.Backend.platform.organization.dto.*;
import com.qs.Backend.platform.organization.service.OrganizationService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping
    public ApiResponse<OrganizationListResponse> list(@RequestParam(required = false) String status, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(organizationService.list(status, limit, offset), null);
    }

    @PostMapping
    public ApiResponse<OrganizationResponse> create(@RequestBody OrganizationCreateRequest request) {
        return ApiResponse.created(organizationService.create(request), "Organization created");
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> get(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(organizationService.getResponseById(id), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<OrganizationResponse> update(@PathVariable java.util.UUID id, @RequestBody OrganizationUpdateRequest request) {
        return ApiResponse.ok(organizationService.update(id, request), "Organization updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable java.util.UUID id) {
        organizationService.delete(id);
        return ApiResponse.ok(null, "Organization deleted");
    }
}
