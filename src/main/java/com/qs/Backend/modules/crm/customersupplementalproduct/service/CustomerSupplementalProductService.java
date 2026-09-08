package com.qs.Backend.modules.crm.customersupplementalproduct.service;

import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.modules.crm.customersupplementalproduct.dto.*;
import com.qs.Backend.modules.inventory.product.entity.Product;
import com.qs.Backend.modules.inventory.product.repository.ProductRepository;
import com.qs.Backend.modules.inventory.productitem.dto.ProductItemActivateRequest;
import com.qs.Backend.modules.inventory.productitem.dto.ProductItemCreateRequest;
import com.qs.Backend.modules.inventory.productitem.entity.ProductItem;
import com.qs.Backend.modules.inventory.productitem.repository.ProductItemRepository;
import com.qs.Backend.modules.inventory.productitem.service.ProductItemService;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerSupplementalProductService {
    private final ProductItemRepository productItemRepository;
    private final ProductItemService productItemService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomerSupplementalProductListResponse listByCustomer(String customerId) {
        ensureCustomer(customerId);
        var items = productItemRepository.findAll((root, query, cb) -> cb.and(cb.equal(root.get("customerId"), customerId), cb.isNull(root.get("deletedAt"))), PageRequest.of(0, 1000)).getContent().stream().map(this::toResponse).toList();
        return CustomerSupplementalProductListResponse.builder().items(items).build();
    }

    @Transactional
    public CustomerSupplementalProductResponse create(String customerId, CustomerSupplementalProductCreateRequest request, UUID userId) {
        ensureCustomer(customerId);
        if (userId == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        UUID productId = resolveProductId(request);
        String code = firstNonBlank(request.getProductItemCode(), request.getChipId(), request.getModelName());
        if (code == null) throw invalid();
        ProductItem item = productItemRepository.findByCodeAndDeletedAtIsNull(code).orElseGet(() -> createItem(code, productId));
        ProductItemActivateRequest activate = new ProductItemActivateRequest();
        activate.setCustomerId(UUID.fromString(customerId));
        activate.setWarrantyExpiry(request.getWarrantyExpiry());
        return toResponse(productItemRepository.findById(productItemService.activate(item.getId(), activate, userId).getId()).orElseThrow());
    }

    private ProductItem createItem(String code, UUID productId) {
        ProductItemCreateRequest request = new ProductItemCreateRequest();
        request.setCode(code);
        request.setProductId(productId);
        return productItemRepository.findById(productItemService.create(request).getId()).orElseThrow();
    }

    private UUID resolveProductId(CustomerSupplementalProductCreateRequest request) {
        if (request.getProductId() != null && productRepository.existsById(request.getProductId())) return request.getProductId();
        String code = firstNonBlank(request.getProductCode(), request.getProductName());
        if (code == null) throw invalid();
        Product product = productRepository.findByCodeAndDeletedAtIsNull(code).orElseGet(() -> createProduct(code));
        return product.getId();
    }

    private Product createProduct(String code) {
        Product product = new Product();
        product.setCode(code);
        product.setSku(code);
        product.setName(code);
        product.setPrice(BigDecimal.ZERO);
        product.setWarrantyMonths(24);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return productRepository.save(product);
    }

    private void ensureCustomer(String customerId) {
        if (customerId == null || customerId.isBlank() || !customerRepository.existsById(customerId)) throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
    }

    private CustomerSupplementalProductResponse toResponse(ProductItem item) {
        String productName = productRepository.findById(item.getProductId()).map(Product::getName).orElse("");
        return CustomerSupplementalProductResponse.builder().id(item.getId()).code(item.getCode()).customerId(item.getCustomerId()).productId(item.getProductId()).productName(productName).modelName(item.getCode()).warrantyExpiry(item.getWarrantyExpiry()).status(item.getStatus()).activatedBy(item.getActivatedBy()).activatedAt(item.getActivatedAt()).createdAt(item.getCreatedAt()).build();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return null;
    }

    private AppException invalid() {
        return new AppException("Invalid supplemental product request", HttpStatus.BAD_REQUEST, "CUSTOMER_SUPPLEMENTAL_PRODUCT_INVALID_REQUEST");
    }
}
