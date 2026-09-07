package com.qs.Backend.modules.crm.businessfield.controller;

import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldCreateRequest;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldListResponse;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldResponse;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldUpdateRequest;
import com.qs.Backend.modules.crm.businessfield.service.BusinessFieldService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BusinessFieldController {
    private final BusinessFieldService businessFieldService;

    @GetMapping({"/business-fields", "/public/business-fields"})
    public ApiResponse<BusinessFieldListResponse> list(@RequestParam(defaultValue = "50") int limit,
                                                        @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(businessFieldService.list(limit, offset), null);
    }

    @GetMapping("/business-fields/{id}")
    public ApiResponse<BusinessFieldResponse> get(@PathVariable String id) {
        return ApiResponse.ok(businessFieldService.get(id), null);
    }

    @PostMapping("/business-fields")
    public ApiResponse<BusinessFieldResponse> create(@RequestBody BusinessFieldCreateRequest request) {
        return ApiResponse.created(businessFieldService.create(request), "Business field created");
    }

    @PatchMapping("/business-fields/{id}")
    public ApiResponse<BusinessFieldResponse> update(@PathVariable String id,
                                                      @RequestBody BusinessFieldUpdateRequest request) {
        return ApiResponse.ok(businessFieldService.update(id, request), "Business field updated");
    }

    @DeleteMapping("/business-fields/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        businessFieldService.delete(id);
        return ApiResponse.ok(null, "Business field deleted");
    }
}
