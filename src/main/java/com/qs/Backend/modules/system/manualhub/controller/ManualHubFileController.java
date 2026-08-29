package com.qs.Backend.modules.system.manualhub.controller;

import com.qs.Backend.modules.system.manualhub.dto.UploadFileResponse;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubFile;
import com.qs.Backend.modules.system.manualhub.service.ManualHubFileService;
import com.qs.Backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ManualHubFileController {

    private final ManualHubFileService fileService;

    @PostMapping("/manualhub/documents/{id}/images")
    public ApiResponse<UploadFileResponse> upload(@PathVariable Long id, @RequestParam("files") MultipartFile files) {
        return ApiResponse.created(fileService.upload(id, files), "Đã tải file lên");
    }

    // Public: fetched directly by the browser (docx preview links) and by the
    // OnlyOffice Document Server container (which has no user JWT).
    @GetMapping("/public/manualhub/documents/{id}/files/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long fileId) {
        ManualHubFile meta = fileService.findMeta(fileId);
        Resource resource = fileService.load(fileId);
        MediaType mediaType = meta.getContentType() != null
                ? MediaType.parseMediaType(meta.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName(meta.getOriginalName()) + "\"")
                .body(resource);
    }

    private static String safeName(String name) {
        return name != null ? name.replace("\"", "") : "document";
    }
}
