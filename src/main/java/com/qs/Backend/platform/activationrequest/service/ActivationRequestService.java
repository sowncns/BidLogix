package com.qs.Backend.platform.activationrequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.Backend.modules.crm.customer.repository.CustomerRepository;
import com.qs.Backend.modules.inventory.productitem.dto.ProductItemBulkActivateRequest;
import com.qs.Backend.modules.inventory.productitem.dto.ProductItemBulkActivateResponse;
import com.qs.Backend.modules.inventory.productitem.service.ProductItemService;
import com.qs.Backend.platform.activationrequest.dto.*;
import com.qs.Backend.platform.activationrequest.entity.ActivationRequest;
import com.qs.Backend.platform.activationrequest.repository.ActivationRequestRepository;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationRequestService {

    private final ActivationRequestRepository activationRequestRepository;
    private final CustomerRepository customerRepository;
    private final ProductItemService productItemService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ActivationRequestResponse submit(ActivationRequestSubmitRequest request, UUID userId) {
        requireUser(userId);
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new AppException("Customer not found", HttpStatus.NOT_FOUND, "CRM_CUSTOMER_NOT_FOUND");
        }
        String requestType = request.getRequestType() == null || request.getRequestType().isBlank()
                ? "activation"
                : request.getRequestType().trim();
        if (!requestType.equals("activation") && !requestType.equals("supplemental_product")) {
            throw new AppException("Invalid request type", HttpStatus.BAD_REQUEST, "ACTIVATION_REQUEST_INVALID_TYPE");
        }

        ActivationRequest entity = new ActivationRequest();
        entity.setRequestType(requestType);
        entity.setSalesUserId(userId);
        entity.setCustomerId(request.getCustomerId());
        entity.setInputs(writeJson(request.getInputs()));
        entity.setWarrantyExpiry(request.getWarrantyExpiry());
        entity.setStatus("pending");
        Instant now = Instant.now();
        entity.setSubmittedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toResponse(activationRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ActivationRequestListResponse list(String status, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        Page<ActivationRequest> page = activationRequestRepository.findAll(spec(status), PageRequest.of(safeOffset / safeLimit, safeLimit));
        return ActivationRequestListResponse.builder()
                .items(page.getContent().stream().map(this::toResponse).toList())
                .total(page.getTotalElements())
                .limit(safeLimit)
                .offset(safeOffset)
                .build();
    }

    @Transactional(readOnly = true)
    public ActivationRequestResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ActivationRequestResponse approve(Long id, UUID reviewerId) {
        requireUser(reviewerId);
        ActivationRequest entity = findOrThrow(id);
        ensurePending(entity);

        ProductItemBulkActivateRequest bulkRequest = new ProductItemBulkActivateRequest();
        bulkRequest.setCustomerId(entity.getCustomerId());
        bulkRequest.setInputs(readInputs(entity.getInputs()));
        bulkRequest.setWarrantyExpiry(entity.getWarrantyExpiry());
        ProductItemBulkActivateResponse result = productItemService.bulkActivate(bulkRequest, reviewerId);

        entity.setStatus("approved");
        entity.setReviewedBy(reviewerId);
        entity.setReviewedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setResult(writeJson(result));
        return toResponse(entity);
    }

    @Transactional
    public ActivationRequestResponse reject(Long id, UUID reviewerId, ActivationRequestRejectRequest request) {
        requireUser(reviewerId);
        ActivationRequest entity = findOrThrow(id);
        ensurePending(entity);
        entity.setStatus("rejected");
        entity.setReviewedBy(reviewerId);
        entity.setReviewedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setRejectReason(request == null ? null : request.getReason());
        return toResponse(entity);
    }

    private Specification<ActivationRequest> spec(String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ActivationRequest findOrThrow(Long id) {
        return activationRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Activation request not found", HttpStatus.NOT_FOUND, "ACTIVATION_REQUEST_NOT_FOUND"));
    }

    private void ensurePending(ActivationRequest entity) {
        if (!"pending".equals(entity.getStatus())) {
            throw new AppException("Activation request is not pending", HttpStatus.CONFLICT, "ACTIVATION_REQUEST_NOT_PENDING");
        }
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        }
    }

    private ActivationRequestResponse toResponse(ActivationRequest entity) {
        return ActivationRequestResponse.builder()
                .id(entity.getId())
                .requestType(entity.getRequestType())
                .salesUserId(entity.getSalesUserId())
                .customerId(entity.getCustomerId())
                .organizationId(entity.getOrganizationId())
                .inputs(readInputs(entity.getInputs()))
                .warrantyExpiry(entity.getWarrantyExpiry())
                .status(entity.getStatus())
                .result(readResult(entity.getResult()))
                .rejectReason(entity.getRejectReason())
                .submittedAt(entity.getSubmittedAt())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .build();
    }

    private List<String> readInputs(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private ProductItemBulkActivateResponse readResult(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ProductItemBulkActivateResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new AppException("Cannot serialize activation request", HttpStatus.INTERNAL_SERVER_ERROR, "ACTIVATION_REQUEST_SERIALIZE_FAILED");
        }
    }
}
