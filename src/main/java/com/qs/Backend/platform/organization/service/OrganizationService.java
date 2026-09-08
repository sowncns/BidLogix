package com.qs.Backend.platform.organization.service;

import com.qs.Backend.platform.organization.dto.*;
import com.qs.Backend.platform.organization.entity.Organization;
import com.qs.Backend.platform.organization.repository.OrganizationRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public OrganizationListResponse list(String status, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var pageable = PageRequest.of(safeOffset / safeLimit, safeLimit);
        var page = status == null || status.isBlank()
                ? organizationRepository.findAll(pageable)
                : organizationRepository.findByStatus(status.trim(), pageable);
        return OrganizationListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).limit(safeLimit).offset(safeOffset).build();
    }

    @Transactional
    public OrganizationResponse create(OrganizationCreateRequest request) {
        String code = required(request.getCode(), "code is required");
        if (organizationRepository.existsByCode(code)) {
            throw new AppException("Organization code already exists", HttpStatus.CONFLICT, "ORGANIZATION_CODE_EXISTS");
        }
        Organization org = new Organization();
        org.setCode(code);
        org.setName(required(request.getName(), "name is required"));
        if (request.getParentId() != null) org.setParent(getById(request.getParentId()));
        org.setPath(required(request.getPath(), "path is required"));
        org.setLevel(deriveLevel(org.getPath()));
        org.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "active" : request.getStatus().trim());
        org.setActive("active".equals(org.getStatus()));
        Instant now = Instant.now();
        org.setCreatedAt(now);
        org.setUpdatedAt(now);
        return toResponse(organizationRepository.save(org));
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getResponseById(Long id) {
        return toResponse(getById(id));
    }

    public Organization getById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new AppException("Tổ chức không tồn tại", HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND"));
    }

    @Transactional
    public OrganizationResponse update(Long id, OrganizationUpdateRequest request) {
        Organization org = getById(id);
        if (request.getCode() != null) org.setCode(required(request.getCode(), "code cannot be empty"));
        if (request.getName() != null) org.setName(required(request.getName(), "name cannot be empty"));
        if (request.getParentId() != null) org.setParent(getById(request.getParentId()));
        if (request.getPath() != null) {
            org.setPath(required(request.getPath(), "path cannot be empty"));
            org.setLevel(deriveLevel(org.getPath()));
        }
        if (request.getStatus() != null) {
            org.setStatus(request.getStatus().trim());
            org.setActive("active".equals(org.getStatus()));
        }
        org.setUpdatedAt(Instant.now());
        return toResponse(org);
    }

    @Transactional
    public void delete(Long id) {
        Organization org = getById(id);
        org.setStatus("inactive");
        org.setActive(false);
        org.setUpdatedAt(Instant.now());
    }

    // Includes the organization itself. Backs the ORGANIZATION_AND_CHILDREN data scope.
    public Set<Long> getSelfAndDescendantIds(Long organizationId) {
        Set<Long> ids = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(organizationId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!ids.add(current)) {
                continue;
            }
            List<Organization> children = organizationRepository.findByParentId(current);
            children.forEach(child -> queue.add(child.getId()));
        }

        return ids;
    }

    private OrganizationResponse toResponse(Organization org) {
        return OrganizationResponse.builder().id(org.getId()).code(org.getCode()).name(org.getName()).parentId(org.getParent() == null ? null : org.getParent().getId()).path(org.getPath()).level(org.getLevel()).status(org.getStatus()).createdAt(org.getCreatedAt()).updatedAt(org.getUpdatedAt()).build();
    }

    private int deriveLevel(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) return 0;
        return Math.max(0, (int) path.chars().filter(ch -> ch == '/').count() - 1);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new AppException(message, HttpStatus.BAD_REQUEST, "ORGANIZATION_INVALID_REQUEST");
        return value.trim();
    }
}
