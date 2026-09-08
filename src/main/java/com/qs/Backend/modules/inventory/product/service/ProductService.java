package com.qs.Backend.modules.inventory.product.service;

import com.qs.Backend.modules.inventory.product.dto.ProductCreateRequest;
import com.qs.Backend.modules.inventory.product.dto.ProductListResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductResponse;
import com.qs.Backend.modules.inventory.product.dto.ProductUpdateRequest;
import com.qs.Backend.modules.inventory.product.entity.Product;
import com.qs.Backend.modules.inventory.product.repository.ProductRepository;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final int DEFAULT_WARRANTY_MONTHS = 24;
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        String code = normalizeRequired(request.getCode(), "code is required");
        if (productRepository.existsByCodeAndDeletedAtIsNull(code)) {
            throw new AppException("Product code already exists", HttpStatus.CONFLICT, "INVENTORY_PRODUCT_CODE_EXISTS");
        }
        Product product = new Product();
        product.setCode(code);
        product.setName(normalizeRequired(request.getName(), "name is required"));
        product.setSpecifications(request.getSpecifications() == null ? new LinkedHashMap<>() : request.getSpecifications());
        product.setWarrantyMonths(request.getWarrantyMonths() == null || request.getWarrantyMonths() <= 0 ? DEFAULT_WARRANTY_MONTHS : request.getWarrantyMonths());
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductListResponse listProducts(String search, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = productRepository.findAll(spec(search), PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ProductListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).limit(safeLimit).offset(safeOffset).total(page.getTotalElements()).build();
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = findActiveOrThrow(id);
        if (request.getName() != null) product.setName(normalizeRequired(request.getName(), "name is required"));
        if (request.getSpecifications() != null) product.setSpecifications(request.getSpecifications());
        if (request.getWarrantyMonths() != null) product.setWarrantyMonths(request.getWarrantyMonths() <= 0 ? DEFAULT_WARRANTY_MONTHS : request.getWarrantyMonths());
        product.setUpdatedAt(Instant.now());
        return toResponse(product);
    }

    @Transactional
    public void deactivateProduct(UUID id) {
        Product product = findActiveOrThrow(id);
        product.setDeletedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
    }

    private Specification<Product> spec(String search) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("code")), pattern), cb.like(cb.lower(root.get("name")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Product findActiveOrThrow(UUID id) {
        return productRepository.findById(id).filter(p -> p.getDeletedAt() == null).orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND"));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder().id(product.getId()).code(product.getCode()).name(product.getName()).specifications(product.getSpecifications()).warrantyMonths(product.getWarrantyMonths()).createdAt(product.getCreatedAt()).updatedAt(product.getUpdatedAt()).deletedAt(product.getDeletedAt()).build();
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) throw new AppException(message, HttpStatus.BAD_REQUEST, "INVENTORY_PRODUCT_INVALID_REQUEST");
        return value.trim();
    }
}
