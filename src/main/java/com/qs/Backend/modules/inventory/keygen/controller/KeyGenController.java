package com.qs.Backend.modules.inventory.keygen.controller;

import com.qs.Backend.modules.inventory.keygen.dto.*;
import com.qs.Backend.modules.inventory.keygen.service.KeyGenService;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/keygen")
@RequiredArgsConstructor
public class KeyGenController {

    private final KeyGenService keyGenService;

    @PostMapping("/factory")
    public ApiResponse<GenerateFactoryResponse> generateFactory(@Valid @RequestBody GenerateFactoryRequest request,
                                                                @AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        return ApiResponse.ok(keyGenService.generateFactory(request, userId), null);
    }

    @PostMapping("/active")
    public ApiResponse<GenerateActiveResponse> generateActive(@Valid @RequestBody GenerateActiveRequest request,
                                                              @AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        return ApiResponse.ok(keyGenService.generateActive(request, userId), null);
    }

    @PostMapping("/machine-lock")
    public ApiResponse<GenerateMachineLockResponse> generateMachineLock(@Valid @RequestBody GenerateMachineLockRequest request,
                                                                        @AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        return ApiResponse.ok(keyGenService.generateMachineLock(request, userId), null);
    }

    @GetMapping("/history")
    public ApiResponse<KeyGenHistoryListResponse> listHistory(
            @RequestParam(name = "key_type", required = false) String keyType,
            @RequestParam(name = "generated_by", required = false) Long generatedBy,
            @RequestParam(name = "organization_id", required = false) String organizationId,
            @RequestParam(name = "product_item_id", required = false) Long productItemId,
            @RequestParam(name = "date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(name = "date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.ok(keyGenService.listHistory(keyType, generatedBy, organizationId, productItemId, dateFrom, dateTo, limit, offset), null);
    }
}
