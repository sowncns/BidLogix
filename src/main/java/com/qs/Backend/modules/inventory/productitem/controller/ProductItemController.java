package com.qs.Backend.modules.inventory.productitem.controller;

import com.qs.Backend.modules.inventory.productitem.dto.*;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.modules.inventory.productitem.service.ProductItemService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/product-items")
@RequiredArgsConstructor
public class ProductItemController {

    private final ProductItemService productItemService;

    @GetMapping
    public ApiResponse<ProductItemListResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(name = "customer_id", required = false) String customerId,
            @RequestParam(name = "product_id", required = false) Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "created_at_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtFrom,
            @RequestParam(name = "created_at_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAtTo,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.ok(productItemService.list(status, customerId, productId, keyword, createdAtFrom, createdAtTo, limit, offset), null);
    }

    @PostMapping
    public ApiResponse<ProductItemResponse> create(@Valid @RequestBody ProductItemCreateRequest request) {
        return ApiResponse.created(productItemService.create(request), "Product item created");
    }

    @PostMapping("/bulk-activate")
    public ApiResponse<ProductItemBulkActivateResponse> bulkActivate(@Valid @RequestBody ProductItemBulkActivateRequest request,
                                                                     @AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        return ApiResponse.ok(productItemService.bulkActivate(request, userId), "Product items processed");
    }

    @PostMapping("/preflight")
    public ApiResponse<ProductItemPreflightResponse> preflight(@RequestBody ProductItemPreflightRequest request,
                                                               @AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        return ApiResponse.ok(productItemService.preflight(request, userId), null);
    }

    @PostMapping("/preflight/release")
    public ApiResponse<Void> releasePreflight(@AuthenticationPrincipal AccountPrincipal principal) {
        Long userId = principal == null ? null : principal.getId();
        productItemService.releasePreflight(userId);
        return ApiResponse.ok(null, "Preflight claims released");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductItemResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productItemService.get(id), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductItemResponse> update(@PathVariable Long id, @RequestBody ProductItemUpdateRequest request) {
        return ApiResponse.ok(productItemService.update(id, request), "Product item updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productItemService.delete(id);
        return ApiResponse.ok(null, "Product item deleted");
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<ProductItemResponse> activate(@PathVariable Long id,
                                                     @Valid @RequestBody ProductItemActivateRequest request,
                                                     @AuthenticationPrincipal AccountPrincipal principal) {
        Long activatedBy = principal == null ? null : principal.getId();
        return ApiResponse.ok(productItemService.activate(id, request, activatedBy), "Product item activated");
    }

    @PostMapping("/{id}/recall")
    public ApiResponse<Void> recall(@PathVariable Long id, @Valid @RequestBody ProductItemNotesRequest request) {
        productItemService.recall(id, request);
        return ApiResponse.ok(null, "Product item recalled");
    }

    @PostMapping("/{id}/refurbish")
    public ApiResponse<Void> refurbish(@PathVariable Long id, @Valid @RequestBody ProductItemNotesRequest request) {
        productItemService.refurbish(id, request);
        return ApiResponse.ok(null, "Product item refurbished");
    }
}
