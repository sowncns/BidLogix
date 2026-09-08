package com.qs.Backend.modules.inventory.productitem.service;

import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.modules.inventory.product.entity.Product;
import com.qs.Backend.modules.inventory.keygen.service.KeyGenService;
import com.qs.Backend.modules.inventory.product.repository.ProductRepository;
import com.qs.Backend.modules.inventory.productitem.dto.*;
import com.qs.Backend.modules.inventory.productitem.entity.ProductItem;
import com.qs.Backend.modules.inventory.productitem.repository.ProductItemRepository;
import com.qs.Backend.modules.inventory.productitemlog.service.ProductItemLogService;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductItemService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "stock", "active", "maintenance", "out_of_order", "decommissioned", "recalled", "refurbished"
    );

    private final ProductItemRepository productItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ProductItemLogService productItemLogService;
    private final ProductItemClaimRegistry claimRegistry;
    private final KeyGenService keyGenService;

    @Transactional
    public ProductItemResponse create(ProductItemCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (productItemRepository.existsByCodeAndDeletedAtIsNull(code)) {
            throw new AppException("Product item code already exists", HttpStatus.CONFLICT, "PRODUCT_ITEM_CODE_EXISTS");
        }
        if (!productRepository.existsById(request.getProductId())) {
            throw new AppException("Product not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND");
        }

        ProductItem item = new ProductItem();
        item.setCode(code);
        item.setProductId(request.getProductId());
        item.setManufacturingDate(request.getManufacturingDate());
        item.setStatus("stock");
        Instant now = Instant.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        ProductItem saved = productItemRepository.save(item);
        productItemLogService.log(saved.getId(), "created", null, "direct", null, "{\"code\":\"" + saved.getCode() + "\"}");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductItemResponse get(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductItemListResponse list(String status, String customerId, Long productId, String keyword,
                                        Instant createdAtFrom, Instant createdAtTo, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        Pageable pageable = PageRequest.of(safeOffset / safeLimit, safeLimit);
        Page<ProductItem> page = productItemRepository.findAll(spec(status, customerId, productId, keyword, createdAtFrom, createdAtTo), pageable);
        return ProductItemListResponse.builder()
                .items(page.getContent().stream().map(this::toResponse).toList())
                .limit(safeLimit)
                .offset(safeOffset)
                .total(page.getTotalElements())
                .build();
    }

    @Transactional
    public ProductItemResponse update(Long id, ProductItemUpdateRequest request) {
        ProductItem item = findActiveOrThrow(id);
        boolean changed = false;
        if (request.getStatus() != null) {
            String status = request.getStatus().trim();
            validateStatus(status);
            item.setStatus(status);
            changed = true;
        }
        if (request.getActivationNotes() != null) {
            item.setActivationNotes(request.getActivationNotes().trim());
            changed = true;
        }
        if (request.getInstallationDate() != null) {
            item.setInstallationDate(request.getInstallationDate());
            changed = true;
        }
        if (request.getWarrantyExpiry() != null) {
            item.setWarrantyExpiry(request.getWarrantyExpiry());
            changed = true;
        }
        if (!changed) {
            throw new AppException("No fields to update", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_NO_FIELDS");
        }
        item.setUpdatedAt(Instant.now());
        return toResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        ProductItem item = findActiveOrThrow(id);
        item.setDeletedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
    }

    @Transactional
    public ProductItemResponse activate(Long id, ProductItemActivateRequest request, Long activatedBy) {
        ProductItem item = findActiveOrThrow(id);
        if (!("stock".equals(item.getStatus()) || "refurbished".equals(item.getStatus())) || item.getCustomerId() != null) {
            throw new AppException("Product item cannot be activated", HttpStatus.CONFLICT, "PRODUCT_ITEM_CANNOT_ACTIVATE");
        }
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
        }

        Instant now = Instant.now();
        Instant installationDate = request.getInstallationDate() != null ? request.getInstallationDate() : now;
        Instant warrantyExpiry = request.getWarrantyExpiry();
        Integer warrantyMonths = request.getWarrantyMonths();
        if (warrantyExpiry == null && warrantyMonths != null && warrantyMonths > 0) {
            warrantyExpiry = installationDate.atZone(ZoneOffset.UTC).plusMonths(warrantyMonths).toInstant();
        }
        if (warrantyExpiry != null && warrantyExpiry.isBefore(installationDate)) {
            throw new AppException("Warranty expiry cannot be before installation date", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_INVALID_WARRANTY_EXPIRY");
        }

        item.setCustomerId(request.getCustomerId());
        item.setActivatedBy(activatedBy);
        item.setActivatedAt(now);
        item.setInstallationDate(installationDate);
        item.setWarrantyExpiry(warrantyExpiry);
        item.setStatus("active");
        item.setActivationNotes(request.getNotes() == null ? null : request.getNotes().trim());
        item.setUpdatedAt(now);
        productItemLogService.log(item.getId(), "activated", activatedBy, "direct", item.getCustomerId(), null);
        return toResponse(item);
    }

    @Transactional
    public void recall(Long id, ProductItemNotesRequest request) {
        ProductItem item = findActiveOrThrow(id);
        if (!"active".equals(item.getStatus())) {
            throw new AppException("Product item cannot be recalled", HttpStatus.CONFLICT, "PRODUCT_ITEM_CANNOT_RECALL");
        }
        String notes = normalizeNotes(request.getNotes());
        Instant now = Instant.now();
        item.setStatus("recalled");
        item.setCustomerId(null);
        item.setActivatedAt(null);
        item.setActivatedBy(null);
        item.setInstallationDate(null);
        item.setWarrantyExpiry(null);
        item.setRecalledAt(now);
        item.setRecallNotes(notes);
        item.setUpdatedAt(now);
        productItemLogService.log(item.getId(), "recalled", null, "direct", null, "{\"notes\":\"" + escapeJson(notes) + "\"}");
    }

    @Transactional
    public void refurbish(Long id, ProductItemNotesRequest request) {
        ProductItem item = findActiveOrThrow(id);
        if (!"recalled".equals(item.getStatus())) {
            throw new AppException("Product item cannot be refurbished", HttpStatus.CONFLICT, "PRODUCT_ITEM_CANNOT_REFURBISH");
        }
        Instant now = Instant.now();
        item.setStatus("refurbished");
        item.setRefurbishedAt(now);
        item.setRefurbishNotes(normalizeNotes(request.getNotes()));
        item.setUpdatedAt(now);
        productItemLogService.log(item.getId(), "refurbished", null, "direct", null, "{\"notes\":\"" + escapeJson(item.getRefurbishNotes()) + "\"}");
    }

    @Transactional(readOnly = true)
    public ProductItem findByCode(String code) {
        return productItemRepository.findByCodeAndDeletedAtIsNull(code)
                .orElse(null);
    }

    @Transactional
    public ProductItemPreflightResponse preflight(ProductItemPreflightRequest request, Long userId) {
        requireUser(userId);
        List<String> inputs = request.getInputs() == null ? List.of() : request.getInputs();
        if (inputs.size() > 200) {
            throw new AppException("Too many inputs (max 200)", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_TOO_MANY_INPUTS");
        }

        List<ParsedDevice> parsedRows = new ArrayList<>();
        List<String> validChipIds = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>();
        for (String input : inputs) {
            ParsedDevice parsed = parseDeviceInput(input);
            if (!parsed.valid()) {
                parsedRows.add(parsed);
                continue;
            }
            boolean duplicate = !seen.add(parsed.chipId());
            ParsedDevice row = parsed.withDuplicate(duplicate);
            parsedRows.add(row);
            if (!duplicate) {
                validChipIds.add(parsed.chipId());
            }
        }

        Map<String, Long> conflicts = claimRegistry.sync(userId, validChipIds);
        List<ProductItemPreflightRowResponse> rows = new ArrayList<>();
        for (ParsedDevice parsed : parsedRows) {
            if (!parsed.valid()) {
                rows.add(ProductItemPreflightRowResponse.builder().input(parsed.input()).status("invalid_format").build());
                continue;
            }
            ProductItemPreflightRowResponse.ProductItemPreflightRowResponseBuilder builder = ProductItemPreflightRowResponse.builder()
                    .input(parsed.input()).chipId(parsed.chipId()).model(parsed.model()).status("ok");
            if (parsed.duplicate()) {
                builder.status("duplicate_in_batch");
            } else if (conflicts.containsKey(parsed.chipId())) {
                builder.status("being_added_by_other").claimedById(conflicts.get(parsed.chipId()));
            } else {
                ProductItem item = findByCode(parsed.chipId());
                if (item != null && "active".equals(item.getStatus())) {
                    builder.status("already_active").productItemId(item.getId()).customerId(item.getCustomerId());
                }
            }
            rows.add(builder.build());
        }
        return ProductItemPreflightResponse.builder().rows(rows).build();
    }

    @Transactional
    public void releasePreflight(Long userId) {
        requireUser(userId);
        claimRegistry.releaseAll(userId);
    }

    @Transactional
    public ProductItemBulkActivateResponse bulkActivate(ProductItemBulkActivateRequest request, Long userId) {
        requireUser(userId);
        if (request.getInputs().size() > 200) {
            throw new AppException("Too many inputs (max 200)", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_TOO_MANY_INPUTS");
        }
        if (request.isReActivate() && (request.getReActivateReason() == null || request.getReActivateReason().isBlank())) {
            throw new AppException("re_activate_reason is required when re_activate is true", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_REACTIVATE_REASON_REQUIRED");
        }
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
        }

        Map<String, Boolean> seen = new HashMap<>();
        List<ProductItemBulkRowResponse> rows = new ArrayList<>();
        int success = 0;
        for (String raw : request.getInputs()) {
            ProductItemBulkRowResponse row = processBulkRow(raw, request, userId, seen);
            if ("success".equals(row.getStatus())) {
                success++;
            }
            rows.add(row);
        }
        return ProductItemBulkActivateResponse.builder()
                .total(rows.size())
                .success(success)
                .failed(rows.size() - success)
                .rows(rows)
                .build();
    }

    private ProductItemBulkRowResponse processBulkRow(String raw, ProductItemBulkActivateRequest request, Long userId, Map<String, Boolean> seen) {
        ParsedDevice parsed = parseDeviceInput(raw);
        if (!parsed.valid()) {
            return bulkError(raw, "invalid_format", null, null);
        }
        if (seen.putIfAbsent(parsed.chipId(), true) != null) {
            return bulkError(raw, "duplicate_in_batch", null, null);
        }
        if (!claimRegistry.acquire(userId, parsed.chipId())) {
            return bulkError(raw, "being_added_by_other", null, null);
        }
        try {
            Long productId = resolveBulkProductId(request.getProductId(), parsed.model());
            ProductItem item = productItemRepository.findByCodeAndDeletedAtIsNull(parsed.chipId()).orElse(null);
            if (item == null) {
                ProductItemCreateRequest createRequest = new ProductItemCreateRequest();
                createRequest.setCode(parsed.chipId());
                createRequest.setProductId(productId);
                item = productItemRepository.findById(create(createRequest).getId()).orElseThrow();
            }
            if ("active".equals(item.getStatus())) {
                if (!request.isReActivate()) {
                    return bulkError(raw, "already_active", productId, item.getId());
                }
                productItemLogService.log(item.getId(), "reactivated", userId, "bulk", item.getCustomerId(),
                        "{\"reason\":\"" + escapeJson(request.getReActivateReason()) + "\"}");
                return ProductItemBulkRowResponse.builder()
                        .input(raw).status("success").productId(productId).productItemId(item.getId())
                        .activeKey(keyGenService.generateActiveWithItemId(parsed.raw(), userId, item.getId(), request.getReActivateReason()))
                        .reActivateReason(request.getReActivateReason())
                        .build();
            }
            ProductItemActivateRequest activateRequest = new ProductItemActivateRequest();
            activateRequest.setCustomerId(request.getCustomerId());
            activateRequest.setWarrantyExpiry(request.getWarrantyExpiry());
            ProductItemResponse activated = activate(item.getId(), activateRequest, userId);
            return ProductItemBulkRowResponse.builder()
                    .input(raw).status("success").productId(productId).productItemId(activated.getId())
                    .activeKey(keyGenService.generateActiveWithItemId(parsed.raw(), userId, activated.getId(), null))
                    .warrantyUntil(activated.getWarrantyExpiry() == null ? null : activated.getWarrantyExpiry().toString())
                    .build();
        } catch (AppException e) {
            return bulkError(raw, "activate_failed", null, null);
        } finally {
            claimRegistry.release(userId, parsed.chipId());
        }
    }

    private Long resolveBulkProductId(Long requestedProductId, String model) {
        if (requestedProductId != null) {
            if (!productRepository.existsById(requestedProductId)) {
                throw new AppException("Product not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND");
            }
            return requestedProductId;
        }
        if (model != null && !model.isBlank()) {
            Product product = productRepository.findBySku(model)
                    .orElseThrow(() -> new AppException("Product model not found", HttpStatus.NOT_FOUND, "INVENTORY_PRODUCT_NOT_FOUND"));
            return product.getId();
        }
        throw new AppException("product_id or model is required", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_PRODUCT_REQUIRED");
    }

    private ProductItemBulkRowResponse bulkError(String raw, String error, Long productId, Long productItemId) {
        return ProductItemBulkRowResponse.builder()
                .input(raw)
                .status("error")
                .error(error)
                .productId(productId)
                .productItemId(productItemId)
                .build();
    }

    private Specification<ProductItem> spec(String status, String customerId, Long productId, String keyword,
                                            Instant createdAtFrom, Instant createdAtTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (status != null && !status.isBlank()) {
                validateStatus(status.trim());
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (customerId != null && !customerId.isBlank()) {
                predicates.add(cb.equal(root.get("customerId"), customerId.trim()));
            }
            if (productId != null) {
                predicates.add(cb.equal(root.get("productId"), productId));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (createdAtFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
            }
            if (createdAtTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ProductItem findActiveOrThrow(Long id) {
        ProductItem item = productItemRepository.findById(id)
                .orElseThrow(() -> new AppException("Product item not found", HttpStatus.NOT_FOUND, "PRODUCT_ITEM_NOT_FOUND"));
        if (item.getDeletedAt() != null) {
            throw new AppException("Product item not found", HttpStatus.NOT_FOUND, "PRODUCT_ITEM_NOT_FOUND");
        }
        return item;
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? "" : code.trim();
        if (normalized.isEmpty() || normalized.length() > 100) {
            throw new AppException("Invalid product item code", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_INVALID_CODE");
        }
        return normalized;
    }

    private String normalizeNotes(String notes) {
        String normalized = notes == null ? "" : notes.trim();
        if (normalized.isEmpty()) {
            throw new AppException("Notes are required", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_NOTES_REQUIRED");
        }
        return normalized;
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new AppException("Invalid product item status", HttpStatus.BAD_REQUEST, "PRODUCT_ITEM_INVALID_STATUS");
        }
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        }
    }

    private ParsedDevice parseDeviceInput(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return ParsedDevice.invalid(raw);
        }
        String[] parts = trimmed.split("-", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            return ParsedDevice.invalid(raw);
        }
        if (parts[0].matches(".*\\s+.*") || parts[1].matches(".*\\s+.*") || parts[2].matches(".*\\s+.*")) {
            return ParsedDevice.invalid(raw);
        }
        if (!"30000".equals(parts[2]) && !"Active".equals(parts[2])) {
            return ParsedDevice.invalid(raw);
        }
        String model = "";
        String chipId = parts[0];
        int underscore = parts[0].indexOf('_');
        if (underscore >= 0) {
            if (underscore == 0 || underscore == parts[0].length() - 1 || parts[0].indexOf('_', underscore + 1) >= 0) {
                return ParsedDevice.invalid(raw);
            }
            model = parts[0].substring(0, underscore);
            chipId = parts[0].substring(underscore + 1);
        }
        return new ParsedDevice(raw, model, chipId, true, false);
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record ParsedDevice(String input, String model, String chipId, boolean valid, boolean duplicate) {
        static ParsedDevice invalid(String input) {
            return new ParsedDevice(input, null, null, false, false);
        }

        ParsedDevice withDuplicate(boolean duplicate) {
            return new ParsedDevice(input, model, chipId, valid, duplicate);
        }

        String raw() {
            return input == null ? "" : input.trim();
        }
    }

    private ProductItemResponse toResponse(ProductItem item) {
        return ProductItemResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .productId(item.getProductId())
                .customerId(item.getCustomerId())
                .manufacturingDate(item.getManufacturingDate())
                .installationDate(item.getInstallationDate())
                .warrantyExpiry(item.getWarrantyExpiry())
                .status(item.getStatus())
                .activatedAt(item.getActivatedAt())
                .activatedBy(item.getActivatedBy())
                .activationNotes(item.getActivationNotes())
                .recalledAt(item.getRecalledAt())
                .recallNotes(item.getRecallNotes())
                .refurbishedAt(item.getRefurbishedAt())
                .refurbishNotes(item.getRefurbishNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .deletedAt(item.getDeletedAt())
                .build();
    }
}
