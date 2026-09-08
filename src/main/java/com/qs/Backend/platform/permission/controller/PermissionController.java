package com.qs.Backend.platform.permission.controller;

import com.qs.Backend.platform.auth.repository.PermissionRepository;
import com.qs.Backend.platform.permission.dto.PermissionListResponse;
import com.qs.Backend.platform.permission.dto.PermissionResponse;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionRepository permissionRepository;

    @GetMapping
    public ApiResponse<PermissionListResponse> list() {
        var items = permissionRepository.findAll(Sort.by("code")).stream()
                .map(p -> PermissionResponse.builder().id(p.getId()).code(p.getCode()).description(p.getDescription()).build())
                .toList();
        return ApiResponse.ok(PermissionListResponse.builder().items(items).build(), null);
    }
}
