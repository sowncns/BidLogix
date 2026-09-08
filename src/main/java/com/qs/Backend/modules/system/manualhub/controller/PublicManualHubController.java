package com.qs.Backend.modules.system.manualhub.controller;

import com.qs.Backend.modules.system.manualhub.dto.ManualHubDocumentListResponse;
import com.qs.Backend.modules.system.manualhub.service.ManualHubDocumentService;
import com.qs.Backend.modules.system.manualhub.service.OnlyOfficeService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicManualHubController {

    private final ManualHubDocumentService documentService;
    private final OnlyOfficeService onlyOfficeService;

    @GetMapping("/public/manualhub/documents")
    public ApiResponse<ManualHubDocumentListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "product_id", required = false) UUID productId,
            @RequestParam(name = "parent_id", required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        return ApiResponse.ok(documentService.list(keyword, "released", productId, parentId, false, limit, offset), null);
    }

    @GetMapping("/public/manualhub/documents/{id}/download-pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        return pdfResponse(onlyOfficeService.downloadPdf(id, true, true));
    }

    @GetMapping("/public/manualhub/documents/{id}/view-pdf")
    public ResponseEntity<Resource> viewPdf(@PathVariable Long id) {
        return pdfResponse(onlyOfficeService.downloadPdf(id, false, false));
    }

    private ResponseEntity<Resource> pdfResponse(OnlyOfficeService.PdfDownload pdf) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, pdf.contentDisposition())
                .header("X-Content-Type-Options", "nosniff")
                .body(pdf.resource());
    }
}
