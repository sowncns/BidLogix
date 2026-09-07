package com.qs.Backend.platform.file.controller;

import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.file.dto.LinkedFileResponse;
import com.qs.Backend.platform.file.service.LinkedFileService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LinkedFileController {
    private final LinkedFileService linkedFileService;

    @PostMapping({"/products/{id}/images", "/activities/{id}/images", "/work-orders/{id}/attachments", "/campaigns/{id}/files"})
    public ApiResponse<LinkedFileResponse> upload(@PathVariable String id, @RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "0") int displayOrder, @AuthenticationPrincipal AccountPrincipal principal, HttpServletRequest request) {
        String path = request.getRequestURI();
        String uploadedBy = principal == null ? null : String.valueOf(principal.getId());
        return ApiResponse.created(linkedFileService.upload(entityType(path), id, purpose(path), file, uploadedBy, displayOrder), "File uploaded");
    }

    @GetMapping({"/products/{id}/images", "/campaigns/{id}/files"})
    public ApiResponse<List<LinkedFileResponse>> list(@PathVariable String id, HttpServletRequest request) {
        String path = request.getRequestURI();
        return ApiResponse.ok(linkedFileService.list(entityType(path), id, purpose(path)), null);
    }

    @GetMapping({"/products/{id}/images/{fileId}", "/public/products/{id}/images/{fileId}", "/activities/{id}/images/{fileId}", "/work-orders/{id}/attachments/{fileId}", "/campaigns/{id}/files/{fileId}"})
    public ResponseEntity<?> download(@PathVariable String id, @PathVariable String fileId, HttpServletRequest request) {
        String path = request.getRequestURI();
        var download = linkedFileService.download(entityType(path), id, fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.fileName() + "\"")
                .body(download.resource());
    }

    @DeleteMapping({"/products/{id}/images/{fileId}", "/activities/{id}/images/{fileId}", "/work-orders/{id}/attachments/{fileId}", "/campaigns/{id}/files/{fileId}"})
    public ApiResponse<Void> delete(@PathVariable String id, @PathVariable String fileId, HttpServletRequest request) {
        String path = request.getRequestURI();
        linkedFileService.delete(entityType(path), id, fileId);
        return ApiResponse.ok(null, "File deleted");
    }

    private String entityType(String path) {
        if (path.contains("/products/")) return "product";
        if (path.contains("/activities/")) return "activity";
        if (path.contains("/work-orders/")) return "work_order";
        return "campaign";
    }

    private String purpose(String path) {
        return path.contains("/attachments") || path.contains("/files") ? "attachment" : "image";
    }
}
