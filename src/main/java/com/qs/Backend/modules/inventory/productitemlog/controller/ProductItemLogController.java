package com.qs.Backend.modules.inventory.productitemlog.controller;

import com.qs.Backend.modules.inventory.productitemlog.dto.ProductItemLogListResponse;
import com.qs.Backend.modules.inventory.productitemlog.service.ProductItemLogService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/product-items/{productItemId}/logs")
@RequiredArgsConstructor
public class ProductItemLogController {

    private final ProductItemLogService productItemLogService;

    @GetMapping
    public ApiResponse<ProductItemLogListResponse> list(@PathVariable UUID productItemId,
                                                        @RequestParam(defaultValue = "50") int limit,
                                                        @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(productItemLogService.list(productItemId, limit, offset), null);
    }
}
