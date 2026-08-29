package com.qs.Backend.platform.permission.service;

import com.qs.Backend.shared.exception.AppException;
import com.qs.Backend.platform.auth.entity.AuthUser;
import com.qs.Backend.platform.auth.entity.Permission;
import com.qs.Backend.platform.auth.entity.Role;
import com.qs.Backend.platform.auth.repository.AccountRepository;
import com.qs.Backend.platform.permission.dto.UserPermissionInfo;
import com.qs.Backend.platform.permission.entity.AccountOrganization;
import com.qs.Backend.platform.permission.entity.DataScope;
import com.qs.Backend.platform.permission.repository.AccountOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final AccountRepository accountRepository;
    private final AccountOrganizationRepository accountOrganizationRepository;

    public UserPermissionInfo getUserPermissionInfo(Long accountId) {
        AuthUser account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        Set<Role> roles = account.getRoles();
        Set<Permission> permissions = new HashSet<>();
        DataScope effectiveScope = DataScope.SELF;
        for (Role role : roles) {
            permissions.addAll(role.getPermissions());
            effectiveScope = DataScope.mostPermissive(effectiveScope, role.getDataScope());
        }

        List<Long> organizationIds = accountOrganizationRepository.findByAccountId(accountId).stream()
                .map(AccountOrganization::getOrganizationId)
                .toList();

        return new UserPermissionInfo(accountId, roles, permissions, effectiveScope, organizationIds);
    }

    public boolean hasPermission(Long accountId, String permissionCode) {
        return getUserPermissionInfo(accountId).hasPermission(permissionCode);
    }
}
