package com.qs.Backend.platform.role.controller;

import com.qs.Backend.platform.role.dto.*;
import com.qs.Backend.platform.role.service.RoleAdminService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleAdminService roleAdminService;

    @GetMapping
    public ApiResponse<RoleListResponse> list(@RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.ok(roleAdminService.list(limit, offset), null);
    }

    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleCreateRequest request) {
        return ApiResponse.created(roleAdminService.create(request), "Role created");
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> get(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(roleAdminService.get(id), null);
    }

    @PatchMapping("/{id}")
    public ApiResponse<RoleResponse> update(@PathVariable java.util.UUID id, @RequestBody RoleUpdateRequest request) {
        return ApiResponse.ok(roleAdminService.update(id, request), "Role updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable java.util.UUID id) {
        roleAdminService.delete(id);
        return ApiResponse.ok(null, "Role deleted");
    }

    @PostMapping("/{id}/permissions")
    public ApiResponse<RoleResponse> assignPermission(@PathVariable java.util.UUID id, @RequestBody AssignPermissionRequest request) {
        return ApiResponse.ok(roleAdminService.assignPermission(id, request), "Permission assigned");
    }

    @DeleteMapping("/{id}/permissions/{permissionCode}")
    public ApiResponse<RoleResponse> revokePermission(@PathVariable java.util.UUID id, @PathVariable String permissionCode) {
        return ApiResponse.ok(roleAdminService.revokePermission(id, permissionCode), "Permission revoked");
    }
}
