package com.qs.Backend.modules.system.manualhub.controller;

import com.qs.Backend.modules.system.manualhub.dto.*;
import com.qs.Backend.modules.system.manualhub.service.ManualHubDocumentService;
import com.qs.Backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manualhub")
@RequiredArgsConstructor
public class ManualHubDocumentController {

    private final ManualHubDocumentService documentService;

    @GetMapping("/documents")
    public ApiResponse<ManualHubDocumentListResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(name = "product_id", required = false) UUID productId,
            @RequestParam(name = "parent_id", required = false) UUID parentId,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        return ApiResponse.ok(documentService.list(keyword, status, productId, parentId, mine, limit, offset), null);
    }

    @GetMapping("/documents/{id}")
    public ApiResponse<ManualHubDocumentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.get(id), null);
    }

    @PostMapping("/documents")
    public ApiResponse<ManualHubDocumentResponse> create(@Valid @RequestBody CreateManualHubDocumentRequest request) {
        return ApiResponse.created(documentService.create(request), "Đã tạo tài liệu");
    }

    @PatchMapping("/documents/{id}")
    public ApiResponse<ManualHubDocumentResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateManualHubDocumentRequest request) {
        return ApiResponse.ok(documentService.update(id, request), "Đã cập nhật tài liệu");
    }

    @PostMapping("/documents/{id}/publish")
    public ApiResponse<ManualHubDocumentResponse> publish(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.publish(id), "Đã xuất bản tài liệu");
    }

    @GetMapping("/documents/{id}/versions")
    public ApiResponse<List<ManualHubDocumentVersionResponse>> listVersions(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.listVersions(id), null);
    }

    @GetMapping("/documents/{id}/versions/{version}")
    public ApiResponse<ManualHubDocumentVersionResponse> getVersion(@PathVariable UUID id, @PathVariable String version) {
        return ApiResponse.ok(documentService.getVersion(id, version), null);
    }

    @GetMapping("/documents/{id}/activities")
    public ApiResponse<List<ManualHubActivityResponse>> listActivities(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.listActivities(id), null);
    }

    @GetMapping("/activities")
    public ApiResponse<List<ManualHubActivityResponse>> listAllActivities() {
        return ApiResponse.ok(documentService.listAllActivities(), null);
    }

    @PostMapping("/documents/{id}/hide")
    public ApiResponse<ManualHubDocumentResponse> hide(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.hide(id), "Đã ẩn tài liệu");
    }

    @PostMapping("/documents/{id}/unhide")
    public ApiResponse<ManualHubDocumentResponse> unhide(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.unhide(id), "Đã bỏ ẩn tài liệu");
    }

    @PostMapping("/documents/{id}/delete-request")
    public ApiResponse<Void> deleteRequest(@PathVariable UUID id) {
        documentService.delete(id, true);
        return ApiResponse.ok(null, "Đã gửi yêu cầu xoá");
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> hardDelete(@PathVariable UUID id) {
        documentService.delete(id, false);
        return ApiResponse.ok(null, "Đã xoá tài liệu");
    }

    @PostMapping("/documents/{id}/delete-approve")
    public ApiResponse<Void> approveDelete(@PathVariable UUID id) {
        documentService.approveDelete(id);
        return ApiResponse.ok(null, "Đã duyệt xoá tài liệu");
    }

    @PostMapping("/documents/{id}/delete-reject")
    public ApiResponse<ManualHubDocumentResponse> rejectDelete(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.rejectDelete(id), "Đã từ chối yêu cầu xoá");
    }

    @PostMapping("/documents/{id}/rollback")
    public ApiResponse<ManualHubDocumentResponse> rollback(@PathVariable UUID id, @Valid @RequestBody RollbackRequest request) {
        return ApiResponse.ok(documentService.rollback(id, request.getReason()), "Đã khôi phục phiên bản trước");
    }

    @GetMapping("/stats")
    public ApiResponse<ManualHubStatsResponse> stats() {
        return ApiResponse.ok(documentService.stats(), null);
    }
}
