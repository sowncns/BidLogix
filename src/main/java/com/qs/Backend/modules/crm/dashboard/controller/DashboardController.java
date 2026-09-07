package com.qs.Backend.modules.crm.dashboard.controller;

import com.qs.Backend.modules.crm.dashboard.dto.DashboardStatsResponse;
import com.qs.Backend.modules.crm.dashboard.service.DashboardService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsResponse> stats() {
        return ApiResponse.ok(dashboardService.stats(), null);
    }
}
