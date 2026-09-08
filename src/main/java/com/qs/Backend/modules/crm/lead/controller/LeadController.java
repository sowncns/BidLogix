package com.qs.Backend.modules.crm.lead.controller;

import com.qs.Backend.modules.crm.lead.dto.*;
import com.qs.Backend.modules.crm.lead.service.LeadService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;

    @GetMapping("/leads")
    public ApiResponse<LeadListResponse> list(@RequestParam(required = false) String search, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(leadService.list(search, limit, offset), null);
    }

    @PostMapping("/leads")
    public ApiResponse<LeadResponse> create(@RequestBody LeadCreateRequest request) {
        return ApiResponse.created(leadService.create(request), "Lead created");
    }

    @PostMapping("/public/leads")
    public ApiResponse<LeadResponse> createPublic(@RequestBody LeadCreateRequest request) {
        return create(request);
    }

    @GetMapping("/leads/{id}")
    public ApiResponse<LeadResponse> get(@PathVariable String id) {
        return ApiResponse.ok(leadService.get(id), null);
    }

    @PatchMapping("/leads/{id}")
    public ApiResponse<LeadResponse> update(@PathVariable String id, @RequestBody LeadUpdateRequest request) {
        return ApiResponse.ok(leadService.update(id, request), "Lead updated");
    }

    @DeleteMapping("/leads/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        leadService.delete(id);
        return ApiResponse.ok(null, "Lead deleted");
    }
}
