package com.qs.Backend.modules.crm.businessfield.service;

import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldCreateRequest;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldListResponse;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldResponse;
import com.qs.Backend.modules.crm.businessfield.dto.BusinessFieldUpdateRequest;
import com.qs.Backend.modules.crm.businessfield.entity.BusinessField;
import com.qs.Backend.modules.crm.businessfield.repository.BusinessFieldRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BusinessFieldService {
    private final BusinessFieldRepository businessFieldRepository;

    @Transactional(readOnly = true)
    public BusinessFieldListResponse list(int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = businessFieldRepository.findAll(PageRequest.of(offset / safeLimit, safeLimit, Sort.by("name")));
        return BusinessFieldListResponse.builder()
                .items(page.map(this::toResponse).getContent())
                .limit(limit)
                .offset(Math.max(offset, 0))
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessFieldResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public BusinessFieldResponse create(BusinessFieldCreateRequest request) {
        String name = trim(request.getName());
        if (name == null || name.isEmpty()) {
            throw invalid("name is required");
        }
        BusinessField field = new BusinessField();
        field.setName(name);
        field.setDescription(trim(request.getDescription()));
        field.setLabelVi(trim(request.getLabelVi()));
        field.setLabelEn(trim(request.getLabelEn()));
        return toResponse(businessFieldRepository.save(field));
    }

    @Transactional
    public BusinessFieldResponse update(String id, BusinessFieldUpdateRequest request) {
        BusinessField field = findOrThrow(id);
        if (request.getName() != null) {
            String name = trim(request.getName());
            if (name == null || name.isEmpty()) {
                throw invalid("name cannot be empty");
            }
            field.setName(name);
        }
        if (request.getDescription() != null) field.setDescription(trim(request.getDescription()));
        if (request.getLabelVi() != null) field.setLabelVi(trim(request.getLabelVi()));
        if (request.getLabelEn() != null) field.setLabelEn(trim(request.getLabelEn()));
        field.setUpdatedAt(Instant.now());
        return toResponse(field);
    }

    @Transactional
    public void delete(String id) {
        BusinessField field = findOrThrow(id);
        businessFieldRepository.delete(field);
    }

    private BusinessField findOrThrow(String id) {
        if (id == null || id.isBlank()) {
            throw invalid("id is required");
        }
        return businessFieldRepository.findById(id)
                .orElseThrow(() -> new AppException("business field not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private AppException invalid(String message) {
        return new AppException("invalid business field: " + message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private BusinessFieldResponse toResponse(BusinessField field) {
        return BusinessFieldResponse.builder()
                .id(field.getId())
                .name(field.getName())
                .description(field.getDescription())
                .labelVi(field.getLabelVi())
                .labelEn(field.getLabelEn())
                .createdAt(field.getCreatedAt())
                .updatedAt(field.getUpdatedAt())
                .build();
    }
}
