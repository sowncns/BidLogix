package com.qs.Backend.modules.inventory.product.service;

import com.qs.Backend.modules.inventory.product.dto.ProductCreateRequest;
import com.qs.Backend.modules.inventory.product.dto.ProductResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductUpdateRequest;
import com.qs.Backend.modules.inventory.product.entity.Product;
import com.qs.Backend.modules.inventory.product.repository.ProductRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new AppException("Product SKU already exists", HttpStatus.CONFLICT, "INVENTORY_PRODUCT_SKU_EXISTS");
        }
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findActiveOrThrow(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setUpdatedAt(Instant.now());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse adjustStock(Long id, int delta) {
        Product product = findActiveOrThrow(id);
        int newQuantity = product.getStockQuantity() + delta;
        if (newQuantity < 0) {
            throw new AppException("Stock quantity cannot go negative", HttpStatus.CONFLICT, "INVENTORY_STOCK_NEGATIVE");
        }
        product.setStockQuantity(newQuantity);
        product.setUpdatedAt(Instant.now());
        return toResponse(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = findActiveOrThrow(id);
        product.setActive(false);
        product.setUpdatedAt(Instant.now());
    }

    private Product findActiveOrThrow(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND"));
        if (!product.isActive()) {
            throw new AppException("Product not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND");
        }
        return product;
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
