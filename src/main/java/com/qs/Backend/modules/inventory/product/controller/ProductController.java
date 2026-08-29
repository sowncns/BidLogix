package com.qs.Backend.modules.inventory.product.controller;

import com.qs.Backend.modules.inventory.product.dto.ProductCreateRequest;
import com.qs.Backend.modules.inventory.product.dto.ProductResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductUpdateRequest;
import com.qs.Backend.modules.inventory.product.dto.StockAdjustRequest;
import com.qs.Backend.modules.inventory.product.service.ProductService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.created(productService.createProduct(request), "Product created");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProduct(id), null);
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> list(Pageable pageable) {
        return ApiResponse.ok(productService.listProducts(pageable), null);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponse.ok(productService.updateProduct(id, request), "Product updated");
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<ProductResponse> adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return ApiResponse.ok(productService.adjustStock(id, request.getDelta()), "Stock adjusted");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ApiResponse.ok(null, "Product deactivated");
    }
}
