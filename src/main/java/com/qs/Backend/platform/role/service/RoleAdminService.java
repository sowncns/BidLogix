package com.qs.Backend.platform.role.service;

import com.qs.Backend.platform.auth.entity.Permission;
import com.qs.Backend.platform.auth.entity.Role;
import com.qs.Backend.platform.auth.repository.PermissionRepository;
import com.qs.Backend.platform.auth.repository.RoleRepository;
import com.qs.Backend.platform.permission.entity.DataScope;
import com.qs.Backend.platform.role.dto.*;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleAdminService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public RoleListResponse list(int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        int safeOffset = Math.max(offset, 0);
        var page = roleRepository.findAll(PageRequest.of(safeOffset / safeLimit, safeLimit, Sort.by("code")));
        return RoleListResponse.builder().items(page.getContent().stream().map(this::toResponse).toList()).total(page.getTotalElements()).limit(safeLimit).offset(safeOffset).build();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(UUID id) {
        return toResponse(findRole(id));
    }

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        String code = required(request.getCode(), "code is required");
        roleRepository.findByCode(code).ifPresent(r -> { throw new AppException("Role code already exists", HttpStatus.CONFLICT, "ROLE_CODE_EXISTS"); });
        Role role = new Role();
        role.setCode(code);
        role.setName(required(request.getName(), "name is required"));
        role.setDataScope(request.getDataScope() == null ? DataScope.SELF : request.getDataScope());
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse update(UUID id, RoleUpdateRequest request) {
        Role role = findRole(id);
        if (request.getName() != null) role.setName(required(request.getName(), "name cannot be empty"));
        if (request.getDataScope() != null) role.setDataScope(request.getDataScope());
        return toResponse(role);
    }

    @Transactional
    public void delete(UUID id) {
        Role role = findRole(id);
        if ("admin".equalsIgnoreCase(role.getCode())) throw new AppException("Cannot delete admin role", HttpStatus.CONFLICT, "ROLE_CANNOT_DELETE_ADMIN");
        roleRepository.delete(role);
    }

    @Transactional
    public RoleResponse assignPermission(UUID id, AssignPermissionRequest request) {
        Role role = findRole(id);
        Permission permission = findPermission(request.getPermissionCode());
        role.getPermissions().add(permission);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse revokePermission(UUID id, String permissionCode) {
        Role role = findRole(id);
        role.getPermissions().removeIf(p -> p.getCode().equals(permissionCode));
        return toResponse(role);
    }

    private Role findRole(UUID id) {
        return roleRepository.findById(id).orElseThrow(() -> new AppException("Role not found", HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND"));
    }

    private Permission findPermission(String code) {
        String normalized = required(code, "permission_code is required");
        return permissionRepository.findByCode(normalized).orElseThrow(() -> new AppException("Permission not found", HttpStatus.NOT_FOUND, "PERMISSION_NOT_FOUND"));
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder().id(role.getId()).code(role.getCode()).name(role.getName()).dataScope(role.getDataScope()).permissions(role.getPermissions().stream().map(p -> PermissionResponse.builder().id(p.getId()).code(p.getCode()).description(p.getDescription()).build()).toList()).build();
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new AppException(message, HttpStatus.BAD_REQUEST, "ROLE_INVALID_REQUEST");
        return value.trim();
    }
}
