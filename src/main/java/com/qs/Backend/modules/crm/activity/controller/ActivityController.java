package com.qs.Backend.modules.crm.activity.controller;

import com.qs.Backend.modules.crm.activity.dto.ActivityCreateRequest;
import com.qs.Backend.modules.crm.activity.dto.ActivityListResponse;
import com.qs.Backend.modules.crm.activity.dto.ActivityResponse;
import com.qs.Backend.modules.crm.activity.dto.ActivityUpdateRequest;
import com.qs.Backend.modules.crm.activity.service.ActivityService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("/customers/{customerId}/activities")
    public ApiResponse<ActivityListResponse> listByCustomer(@PathVariable("customerId") String customerId,
                                                             @RequestParam(defaultValue = "50") int limit,
                                                             @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(activityService.listByCustomer(customerId, limit, offset), null);
    }

    @PostMapping("/customers/{customerId}/activities")
    public ApiResponse<ActivityResponse> createForCustomer(@PathVariable("customerId") String customerId,
                                                            @RequestBody ActivityCreateRequest request,
                                                            @AuthenticationPrincipal AccountPrincipal principal) {
        String salesId = principal == null ? null : String.valueOf(principal.getId());
        return ApiResponse.created(activityService.createForCustomer(customerId, request, salesId), "Activity created");
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<ActivityResponse> get(@PathVariable String id) {
        return ApiResponse.ok(activityService.get(id), null);
    }

    @PatchMapping("/activities/{id}")
    public ApiResponse<ActivityResponse> update(@PathVariable String id,
                                                 @RequestBody ActivityUpdateRequest request) {
        return ApiResponse.ok(activityService.update(id, request), "Activity updated");
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        activityService.delete(id);
        return ApiResponse.ok(null, "Activity deleted");
    }
}
