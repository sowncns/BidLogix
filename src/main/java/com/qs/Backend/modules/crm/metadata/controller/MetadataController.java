package com.qs.Backend.modules.crm.metadata.controller;

import com.qs.Backend.modules.crm.metadata.service.MetadataService;
import com.qs.Backend.shared.exception.AppException;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
@RequiredArgsConstructor
public class MetadataController {
    private final MetadataService metadataService;

    @GetMapping("/{domain}")
    public ApiResponse<?> getMetadata(@PathVariable String domain) {
        return switch (domain) {
            case "customer" -> ApiResponse.ok(metadataService.customerMetadata(), null);
            case "lead" -> ApiResponse.ok(metadataService.leadMetadata(), null);
            default -> throw new AppException("domain not found", HttpStatus.NOT_FOUND, "NOT_FOUND");
        };
    }

    @GetMapping("/users")
    public ApiResponse<?> users() {
        return ApiResponse.ok(metadataService.usersMetadata(), null);
    }
}
