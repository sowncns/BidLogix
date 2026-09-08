package com.qs.Backend.modules.system.manualhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.Backend.modules.inventory.product.entity.Product;
import com.qs.Backend.modules.inventory.product.repository.ProductRepository;
import com.qs.Backend.modules.system.manualhub.dto.*;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubActivity;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocumentVersion;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubFile;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubActivityRepository;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentRepository;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentVersionRepository;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubFileRepository;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManualHubDocumentService {

    private final ManualHubDocumentRepository documentRepository;
    private final ManualHubDocumentVersionRepository versionRepository;
    private final ManualHubActivityRepository activityRepository;
    private final ManualHubFileRepository fileRepository;
    private final ProductRepository productRepository;
    private final ManualHubFileService fileService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ManualHubDocumentListResponse list(String keyword, String status, UUID productId, Long parentId,
                                               boolean mine, int limit, int offset) {
        
        int pageSize = limit > 0 ? limit : 20;
        int page = offset > 0 ? offset / pageSize : 0;
        
        UUID authorId = mine ? currentUserId() : null;
        Page<ManualHubDocument> result = documentRepository.search(
                blankToNull(keyword), blankToNull(status), productId, parentId, authorId,
                PageRequest.of(page, pageSize));
        return ManualHubDocumentListResponse.builder()
                .items(result.getContent().stream().map(this::toResponse).toList())
                .limit(pageSize)
                .offset(offset)
                .total(result.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public ManualHubDocumentResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ManualHubDocumentResponse create(CreateManualHubDocumentRequest request) {
        ManualHubDocument document = new ManualHubDocument();
        document.setProductId(request.getProductId());
        document.setParentId(request.getParentId());
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setDocumentType(request.getDocumentType());
        document.setFormat(request.getFormat());
        document.setLanguage(request.getLanguage() != null ? request.getLanguage() : "vi");
        document.setVersion(request.getVersion() != null ? request.getVersion() : "1");
        document.setContent(writeContent(request.getContent()));
        document.setStatus("draft");
        document.setAuthorId(currentUserId());
        document.setAuthorName(currentUsername());
        document.setCreatedAt(Instant.now());
        document = documentRepository.save(document);

        // No file supplied by the caller (the create form never uploads one
        // up front) and this is a docx — generate a blank one so the
        // document already has a real, server-fetchable fileUrl and opens
        // straight in OnlyOffice instead of the client-side fallback editor.
        if ("docx".equalsIgnoreCase(document.getFormat()) && request.getContent() == null) {
            ManualHubFile blank = fileService.createBlankDocx(document.getId(), document.getTitle());
            String fileUrl = fileService.publicUrl(document.getId(), blank.getId());
            document.setContent(writeContent(java.util.Map.of("file_url", fileUrl)));
        }

        recordActivity(document.getId(), "create", "Tạo tài liệu");
        return toResponse(document);
    }

    @Transactional
    public ManualHubDocumentResponse update(Long id, UpdateManualHubDocumentRequest request) {
        ManualHubDocument document = findOrThrow(id);
        if (request.getTitle() != null) document.setTitle(request.getTitle());
        if (request.getDescription() != null) document.setDescription(request.getDescription());
        if (request.getDocumentType() != null) document.setDocumentType(request.getDocumentType());
        if (request.getFormat() != null) document.setFormat(request.getFormat());
        if (request.getLanguage() != null) document.setLanguage(request.getLanguage());
        if (request.getVersion() != null) document.setVersion(request.getVersion());
        if (request.getContent() != null) document.setContent(writeContent(request.getContent()));
        if (request.getStatus() != null) document.setStatus(request.getStatus());
        if (request.getRejectionReason() != null) document.setRejectionReason(request.getRejectionReason());
        document.setUpdatedAt(Instant.now());
        recordActivity(document.getId(), "update", "Cập nhật tài liệu");
        return toResponse(document);
    }

    @Transactional
    public ManualHubDocumentResponse publish(Long id) {
        ManualHubDocument document = findOrThrow(id);
        if (document.getParentId() != null) {
            documentRepository.findById(document.getParentId()).ifPresent(parent -> {
                parent.setCurrent(false);
                parent.setUpdatedAt(Instant.now());
            });
        }
        document.setStatus("released");
        document.setCurrent(true);
        document.setReleasedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        snapshotVersion(document);
        recordActivity(document.getId(), "publish", "Xuất bản tài liệu");
        return toResponse(document);
    }

    @Transactional
    public ManualHubDocumentResponse hide(Long id) {
        ManualHubDocument document = findOrThrow(id);
        document.setStatus("hidden");
        document.setUpdatedAt(Instant.now());
        recordActivity(document.getId(), "hide", "Ẩn tài liệu");
        return toResponse(document);
    }

    @Transactional
    public ManualHubDocumentResponse unhide(Long id) {
        ManualHubDocument document = findOrThrow(id);
        document.setStatus("released");
        document.setUpdatedAt(Instant.now());
        recordActivity(document.getId(), "unhide", "Bỏ ẩn tài liệu");
        return toResponse(document);
    }

    @Transactional
    public void delete(Long id, boolean asRequest) {
        ManualHubDocument document = findOrThrow(id);
        if (asRequest) {
            document.setStatus("pending_delete");
            document.setUpdatedAt(Instant.now());
            recordActivity(document.getId(), "delete-request", "Yêu cầu xoá tài liệu");
            return;
        }
        hardDelete(document);
    }

    @Transactional
    public ManualHubDocumentResponse approveDelete(Long id) {
        ManualHubDocument document = findOrThrow(id);
        hardDelete(document);
        return null;
    }

    @Transactional
    public ManualHubDocumentResponse rejectDelete(Long id) {
        ManualHubDocument document = findOrThrow(id);
        document.setStatus(document.getReleasedAt() != null ? "released" : "draft");
        document.setUpdatedAt(Instant.now());
        recordActivity(document.getId(), "delete-reject", "Từ chối yêu cầu xoá");
        return toResponse(document);
    }

    @Transactional
    public ManualHubDocumentResponse rollback(Long id, String reason) {
        ManualHubDocument document = findOrThrow(id);
        if (document.getParentId() == null) {
            throw new AppException("Tài liệu không có phiên bản trước để khôi phục", HttpStatus.BAD_REQUEST, "MANUALHUB_NO_PARENT_VERSION");
        }
        ManualHubDocument parent = documentRepository.findById(document.getParentId())
                .orElseThrow(() -> new AppException("Không tìm thấy phiên bản trước", HttpStatus.NOT_FOUND, "MANUALHUB_PARENT_NOT_FOUND"));
        parent.setCurrent(true);
        parent.setUpdatedAt(Instant.now());
        document.setCurrent(false);
        document.setStatus("needs_fix");
        document.setRejectionReason(reason);
        document.setUpdatedAt(Instant.now());
        recordActivity(document.getId(), "rollback", reason);
        return toResponse(parent);
    }

    @Transactional(readOnly = true)
    public List<ManualHubDocumentVersionResponse> listVersions(Long documentId) {
        return versionRepository.findByDocumentIdOrderByCreatedAtDesc(documentId).stream()
                .map(this::toVersionResponse).toList();
    }

    @Transactional(readOnly = true)
    public ManualHubDocumentVersionResponse getVersion(Long documentId, String version) {
        ManualHubDocumentVersion entity = versionRepository.findByDocumentIdAndVersion(documentId, version)
                .orElseThrow(() -> new AppException("Không tìm thấy phiên bản", HttpStatus.NOT_FOUND, "MANUALHUB_VERSION_NOT_FOUND"));
        return toVersionResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ManualHubActivityResponse> listActivities(Long documentId) {
        ManualHubDocument document = findOrThrow(documentId);
        return activityRepository.findByDocumentIdOrderByCreatedAtDesc(documentId).stream()
                .map(a -> toActivityResponse(a, document.getTitle(), productName(document.getProductId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManualHubActivityResponse> listAllActivities() {
        return activityRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(a -> {
                    ManualHubDocument document = documentRepository.findById(a.getDocumentId()).orElse(null);
                    String title = document != null ? document.getTitle() : null;
                    String product = document != null ? productName(document.getProductId()) : null;
                    return toActivityResponse(a, title, product);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ManualHubStatsResponse stats() {
        return ManualHubStatsResponse.builder()
                .released(documentRepository.countByStatus("released"))
                .drafts(documentRepository.countByStatus("draft"))
                .waitingReview(documentRepository.countByStatus("review"))
                .rejected(documentRepository.countByStatus("rejected"))
                .build();
    }

    // -----------------------------------------------------------------
    // internal helpers
    // -----------------------------------------------------------------

    void bumpFileCount(Long documentId) {
        ManualHubDocument document = findOrThrow(documentId);
        document.setFileCount(document.getFileCount() + 1);
        document.setUpdatedAt(Instant.now());
    }

    ManualHubDocument findOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
    }

    private void hardDelete(ManualHubDocument document) {
        fileRepository.deleteAll(fileRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId()));
        versionRepository.deleteAll(versionRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId()));
        activityRepository.deleteAll(activityRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId()));
        documentRepository.delete(document);
    }

    private void snapshotVersion(ManualHubDocument document) {
        ManualHubDocumentVersion version = new ManualHubDocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersion(document.getVersion());
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setCreatedByName(currentUsername());
        version.setCreatedAt(Instant.now());
        versionRepository.save(version);
    }

    private void recordActivity(Long documentId, String action, String content) {
        ManualHubActivity activity = new ManualHubActivity();
        activity.setDocumentId(documentId);
        activity.setActorId(currentUserId());
        activity.setActorName(currentUsername());
        activity.setAction(action);
        activity.setContent(content);
        activity.setCreatedAt(Instant.now());
        activityRepository.save(activity);
    }

    private String productName(UUID productId) {
        if (productId == null) return null;
        return productRepository.findById(productId).map(Product::getName).orElse(null);
    }

    private ManualHubDocumentResponse toResponse(ManualHubDocument d) {
        return ManualHubDocumentResponse.builder()
                .id(d.getId())
                .parentId(d.getParentId())
                .productId(d.getProductId())
                .productName(productName(d.getProductId()))
                .title(d.getTitle())
                .description(d.getDescription())
                .documentType(d.getDocumentType())
                .format(d.getFormat())
                .content(readContent(d.getContent()))
                .status(d.getStatus())
                .language(d.getLanguage())
                .version(d.getVersion())
                .authorName(d.getAuthorName())
                .current(d.isCurrent())
                .submittedAt(d.getSubmittedAt())
                .releasedAt(d.getReleasedAt())
                .updatedAt(d.getUpdatedAt())
                .fileCount(d.getFileCount())
                .rejectionReason(d.getRejectionReason())
                .build();
    }

    private ManualHubDocumentVersionResponse toVersionResponse(ManualHubDocumentVersion v) {
        return ManualHubDocumentVersionResponse.builder()
                .id(v.getId())
                .documentId(v.getDocumentId())
                .version(v.getVersion())
                .title(v.getTitle())
                .content(readContent(v.getContent()))
                .fileUrl(v.getFileUrl())
                .createdByName(v.getCreatedByName())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private ManualHubActivityResponse toActivityResponse(ManualHubActivity a, String documentTitle, String productName) {
        return ManualHubActivityResponse.builder()
                .id(a.getId())
                .documentId(a.getDocumentId())
                .documentTitle(documentTitle)
                .productName(productName)
                .actorId(a.getActorId())
                .actorName(a.getActorName())
                .action(a.getAction())
                .content(a.getContent())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private Object readContent(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String writeContent(Object content) {
        if (content == null) return null;
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            return null;
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private java.util.UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return principal instanceof AccountPrincipal ap ? ap.getId() : null;
    }

    private String currentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        return principal instanceof AccountPrincipal ap ? ap.getUsername() : null;
    }
}
