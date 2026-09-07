package com.qs.Backend.modules.crm.customergroup.service;

import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupListResponse;
import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupRequest;
import com.qs.Backend.modules.crm.customergroup.dto.CustomerGroupResponse;
import com.qs.Backend.modules.crm.customergroup.entity.CustomerGroup;
import com.qs.Backend.modules.crm.customergroup.repository.CustomerGroupRepository;
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
public class CustomerGroupService {
    private final CustomerGroupRepository customerGroupRepository;

    @Transactional(readOnly = true)
    public CustomerGroupListResponse list(int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        var page = customerGroupRepository.findAll(PageRequest.of(offset / safeLimit, safeLimit, Sort.by("name")));
        return CustomerGroupListResponse.builder().items(page.map(this::toResponse).getContent()).limit(limit).offset(Math.max(offset, 0)).build();
    }

    @Transactional(readOnly = true)
    public CustomerGroupResponse get(String id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public CustomerGroupResponse create(CustomerGroupRequest request) {
        String name = trim(request.getName());
        if (name == null || name.isEmpty()) throw invalid("name is required");
        CustomerGroup group = new CustomerGroup();
        group.setName(name);
        group.setDescription(trim(request.getDescription()));
        group.setLabelVi(trim(request.getLabelVi()));
        group.setLabelEn(trim(request.getLabelEn()));
        return toResponse(customerGroupRepository.save(group));
    }

    @Transactional
    public CustomerGroupResponse update(String id, CustomerGroupRequest request) {
        CustomerGroup group = findOrThrow(id);
        if (request.getName() != null) {
            String name = trim(request.getName());
            if (name == null || name.isEmpty()) throw invalid("name cannot be empty");
            group.setName(name);
        }
        if (request.getDescription() != null) group.setDescription(trim(request.getDescription()));
        if (request.getLabelVi() != null) group.setLabelVi(trim(request.getLabelVi()));
        if (request.getLabelEn() != null) group.setLabelEn(trim(request.getLabelEn()));
        group.setUpdatedAt(Instant.now());
        return toResponse(group);
    }

    @Transactional
    public void delete(String id) {
        customerGroupRepository.delete(findOrThrow(id));
    }

    private CustomerGroup findOrThrow(String id) {
        if (id == null || id.isBlank()) throw invalid("id is required");
        return customerGroupRepository.findById(id).orElseThrow(() -> new AppException("customer group not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private AppException invalid(String message) {
        return new AppException("invalid customer group: " + message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private CustomerGroupResponse toResponse(CustomerGroup group) {
        return CustomerGroupResponse.builder().id(group.getId()).name(group.getName()).description(group.getDescription()).labelVi(group.getLabelVi()).labelEn(group.getLabelEn()).createdAt(group.getCreatedAt()).updatedAt(group.getUpdatedAt()).build();
    }
}
