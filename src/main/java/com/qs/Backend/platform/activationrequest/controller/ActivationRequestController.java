package com.qs.Backend.platform.activationrequest.controller;

import com.qs.Backend.platform.activationrequest.dto.*;
import com.qs.Backend.platform.activationrequest.service.ActivationRequestService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/product-items/activation-requests")
@RequiredArgsConstructor
public class ActivationRequestController {

    private final ActivationRequestService activationRequestService;

    @GetMapping
    public ApiResponse<ActivationRequestListResponse> list(@RequestParam(required = false) String status,
                                                           @RequestParam(defaultValue = "50") int limit,
                                                           @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(activationRequestService.list(status, limit, offset), null);
    }

    @PostMapping
    public ApiResponse<ActivationRequestResponse> submit(@Valid @RequestBody ActivationRequestSubmitRequest request,
                                                         @AuthenticationPrincipal AccountPrincipal principal) {
        UUID userId = principal == null ? null : principal.getId();
        return ApiResponse.created(activationRequestService.submit(request, userId), "Activation request submitted");
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivationRequestResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(activationRequestService.get(id), null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ActivationRequestResponse> approve(@PathVariable Long id,
                                                          @AuthenticationPrincipal AccountPrincipal principal) {
        UUID reviewerId = principal == null ? null : principal.getId();
        return ApiResponse.ok(activationRequestService.approve(id, reviewerId), "Activation request approved");
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<ActivationRequestResponse> reject(@PathVariable Long id,
                                                         @RequestBody(required = false) ActivationRequestRejectRequest request,
                                                         @AuthenticationPrincipal AccountPrincipal principal) {
        UUID reviewerId = principal == null ? null : principal.getId();
        return ApiResponse.ok(activationRequestService.reject(id, reviewerId, request), "Activation request rejected");
    }
}
