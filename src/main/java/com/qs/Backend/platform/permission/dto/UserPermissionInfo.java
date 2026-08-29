package com.qs.Backend.platform.permission.dto;

import com.qs.Backend.platform.auth.entity.Permission;
import com.qs.Backend.platform.auth.entity.Role;
import com.qs.Backend.platform.permission.entity.DataScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class UserPermissionInfo {
    private Long accountId;
    private Set<Role> roles;
    private Set<Permission> permissions;
    private DataScope dataScope;
    private List<Long> organizationIds;

    public boolean hasPermission(String code) {
        return permissions.stream().anyMatch(p -> p.getCode().equals(code));
    }
}
