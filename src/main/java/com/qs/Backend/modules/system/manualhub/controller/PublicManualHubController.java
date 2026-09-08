package com.qs.Backend.modules.system.manualhub.controller;

import com.qs.Backend.modules.system.manualhub.dto.ManualHubDocumentListResponse;
import com.qs.Backend.modules.system.manualhub.service.ManualHubDocumentService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicManualHubController {

    private final ManualHubDocumentService documentService;

    @GetMapping("/public/manualhub/documents")
    public ApiResponse<ManualHubDocumentListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "product_id", required = false) Long productId,
            @RequestParam(name = "parent_id", required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        return ApiResponse.ok(documentService.list(keyword, "released", productId, parentId, false, limit, offset), null);
    }
}
