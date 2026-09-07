package com.qs.Backend.modules.crm.customerview.controller;

import com.qs.Backend.modules.crm.customerview.dto.RecordViewRequest;
import com.qs.Backend.modules.crm.customerview.service.CustomerViewService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerViewController {
    private final CustomerViewService customerViewService;

    @PostMapping("/customer-views")
    public ApiResponse<Void> recordView(@RequestBody RecordViewRequest request, @AuthenticationPrincipal AccountPrincipal principal) {
        String salesId = principal == null ? null : String.valueOf(principal.getId());
        customerViewService.recordView(request.getCustomerId(), salesId);
        return ApiResponse.ok(null, "Customer view recorded");
    }
}
