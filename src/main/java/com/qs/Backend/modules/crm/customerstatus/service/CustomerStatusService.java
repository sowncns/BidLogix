package com.qs.Backend.modules.crm.customerstatus.service;

import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusListResponse;
import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusRequest;
import com.qs.Backend.modules.crm.customerstatus.dto.CustomerStatusResponse;
import com.qs.Backend.modules.crm.customerstatus.entity.CustomerStatus;
import com.qs.Backend.modules.crm.customerstatus.repository.CustomerStatusRepository;
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
public class CustomerStatusService {
    private final CustomerStatusRepository customerStatusRepository;

    @Transactional(readOnly = true)
    public CustomerStatusListResponse list(int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = customerStatusRepository.findAll(PageRequest.of(offset / safeLimit, safeLimit, Sort.by("name")));
        return CustomerStatusListResponse.builder().items(page.map(this::toResponse).getContent()).limit(limit).offset(Math.max(offset, 0)).build();
    }

    @Transactional(readOnly = true)
    public CustomerStatusResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public CustomerStatusResponse getByName(String name) {
        return toResponse(customerStatusRepository.findByNameIgnoreCase(name).orElseThrow(() -> new AppException("customer status not found", HttpStatus.NOT_FOUND, "NOT_FOUND")));
    }

    @Transactional
    public CustomerStatusResponse create(CustomerStatusRequest request) {
        String name = trim(request.getName());
        if (name == null || name.isEmpty()) throw invalid("name is required");
        CustomerStatus status = new CustomerStatus();
        status.setName(name);
        status.setDescription(trim(request.getDescription()));
        status.setLabelVi(trim(request.getLabelVi()));
        status.setLabelEn(trim(request.getLabelEn()));
        return toResponse(customerStatusRepository.save(status));
    }

    @Transactional
    public CustomerStatusResponse update(String id, CustomerStatusRequest request) {
        CustomerStatus status = findOrThrow(id);
        if (request.getName() != null) {
            String name = trim(request.getName());
            if (name == null || name.isEmpty()) throw invalid("name cannot be empty");
            status.setName(name);
        }
        if (request.getDescription() != null) status.setDescription(trim(request.getDescription()));
        if (request.getLabelVi() != null) status.setLabelVi(trim(request.getLabelVi()));
        if (request.getLabelEn() != null) status.setLabelEn(trim(request.getLabelEn()));
        status.setUpdatedAt(Instant.now());
        return toResponse(status);
    }

    @Transactional
    public void delete(String id) {
        customerStatusRepository.delete(findOrThrow(id));
    }

    private CustomerStatus findOrThrow(String id) {
        if (id == null || id.isBlank()) throw invalid("id is required");
        return customerStatusRepository.findById(id).orElseThrow(() -> new AppException("customer status not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private AppException invalid(String message) {
        return new AppException("invalid customer status: " + message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private CustomerStatusResponse toResponse(CustomerStatus status) {
        return CustomerStatusResponse.builder().id(status.getId()).name(status.getName()).description(status.getDescription()).labelVi(status.getLabelVi()).labelEn(status.getLabelEn()).createdAt(status.getCreatedAt()).updatedAt(status.getUpdatedAt()).build();
    }
}
