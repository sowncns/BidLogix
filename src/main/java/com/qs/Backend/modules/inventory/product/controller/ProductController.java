package com.qs.Backend.modules.inventory.product.controller;

import com.qs.Backend.modules.inventory.product.dto.ProductCreateRequest;
import com.qs.Backend.modules.inventory.product.dto.ProductListResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductUpdateRequest;
import com.qs.Backend.modules.inventory.product.service.ProductService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ApiResponse.created(productService.createProduct(request), "Product created");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(productService.getProduct(id), null);
    }

    @GetMapping
    public ApiResponse<ProductListResponse> list(@RequestParam(required = false) String search,
                                                 @RequestParam(defaultValue = "50") int limit,
                                                 @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(productService.listProducts(search, limit, offset), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        return ApiResponse.ok(productService.updateProduct(id, request), "Product updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        productService.deactivateProduct(id);
        return ApiResponse.ok(null, "Product deactivated");
    }
}
