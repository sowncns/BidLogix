package com.qs.Backend.modules.system.manualhub.service;

import com.qs.Backend.modules.system.manualhub.dto.UploadFileResponse;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentRepository;
import com.qs.Backend.platform.auth.security.AccountPrincipal;
import com.qs.Backend.platform.file.FileStorageService;
import com.qs.Backend.platform.file.entity.FileLink;
import com.qs.Backend.platform.file.entity.StoredFile;
import com.qs.Backend.platform.file.repository.FileLinkRepository;
import com.qs.Backend.platform.file.repository.StoredFileRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManualHubFileService {
    static final String ENTITY_TYPE = "manual_document";
    static final String PURPOSE = "attachment";

    @Value("${app.public-base-url:http://localhost:${server.port}${server.servlet.context-path:}}")
    private String publicBaseUrl;

    private final FileStorageService storageService;
    private final StoredFileRepository fileRepository;
    private final FileLinkRepository linkRepository;
    private final ManualHubDocumentRepository documentRepository;

    @Transactional
    public UploadFileResponse upload(UUID documentId, MultipartFile file) {
        ManualHubDocument document = document(documentId);
        UUID uploadedBy = currentUserId();
        if (uploadedBy == null) throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        StoredFile stored = saveAndLink(documentId, storageService.storeFile(file, "manualhub/" + documentId),
                file.getOriginalFilename(), file.getContentType(), file.getSize(), uploadedBy);
        bumpCount(document);
        return response(documentId, stored.getId());
    }

    @Transactional
    public StoredFile storeFromUrl(UUID documentId, String sourceUrl, String name, String contentType) {
        try {
            HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(sourceUrl)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IOException("HTTP " + response.statusCode());
            return storeBytes(documentId, response.body(), name, contentType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw fetchFailed();
        } catch (IOException e) {
            throw fetchFailed();
        }
    }

    @Transactional
    public StoredFile storeBytes(UUID documentId, byte[] bytes, String name, String contentType) {
        ManualHubDocument document = document(documentId);
        UUID uploadedBy = document.getAuthorId() != null ? document.getAuthorId() : currentUserId();
        if (uploadedBy == null) throw new AppException("Không xác định được người tải file", HttpStatus.BAD_REQUEST, "MANUALHUB_UPLOADER_REQUIRED");
        StoredFile stored = saveAndLink(documentId, storageService.storeBytes(bytes, name, "manualhub/" + documentId),
                name, contentType, bytes.length, uploadedBy);
        bumpCount(document);
        return stored;
    }

    @Transactional
    public StoredFile createBlankDocx(UUID documentId, String title) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText(title == null ? "" : title);
            doc.write(out);
            String name = (title == null || title.isBlank() ? "document" : title) + ".docx";
            return storeBytes(documentId, out.toByteArray(), name,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } catch (IOException e) {
            throw new AppException("Không tạo được file docx trống", HttpStatus.INTERNAL_SERVER_ERROR, "MANUALHUB_BLANK_DOCX_FAILED");
        }
    }

    @Transactional(readOnly = true)
    public List<StoredFile> list(UUID documentId) {
        return linkRepository.findByEntityTypeAndEntityIdAndPurposeOrderByDisplayOrderAscCreatedAtAsc(ENTITY_TYPE, documentId, PURPOSE)
                .stream().map(FileLink::getFileId).map(this::activeFile).toList();
    }

    @Transactional(readOnly = true)
    public StoredFile findMeta(UUID documentId, UUID fileId) {
        linkRepository.findByEntityTypeAndEntityIdAndFileId(ENTITY_TYPE, documentId, fileId)
                .orElseThrow(() -> new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "MANUALHUB_FILE_NOT_FOUND"));
        return activeFile(fileId);
    }

    public Resource load(UUID documentId, UUID fileId) {
        return storageService.loadAsResource(findMeta(documentId, fileId).getStorageKey());
    }

    @Transactional
    public void deleteAllLinked(UUID documentId) {
        for (FileLink link : linkRepository.findByEntityTypeAndEntityId(ENTITY_TYPE, documentId)) {
            UUID fileId = link.getFileId();
            linkRepository.delete(link);
            if (linkRepository.countByFileId(fileId) == 0) fileRepository.findById(fileId).ifPresent(file -> file.setDeletedAt(Instant.now()));
        }
    }

    public String publicUrl(UUID documentId, UUID fileId) {
        return publicBaseUrl + "/public/manualhub/documents/" + documentId + "/files/" + fileId;
    }

    private StoredFile saveAndLink(UUID documentId, String key, String name, String mime, long size, UUID uploadedBy) {
        StoredFile stored = new StoredFile();
        stored.setOriginalName(name == null ? "document" : name);
        stored.setStorageKey(key);
        stored.setMimeType(mime == null ? "application/octet-stream" : mime);
        stored.setSizeBytes(size);
        stored.setVisibility("internal");
        stored.setUploadedBy(uploadedBy);
        fileRepository.save(stored);
        FileLink link = new FileLink();
        link.setFileId(stored.getId());
        link.setEntityType(ENTITY_TYPE);
        link.setEntityId(documentId);
        link.setPurpose(PURPOSE);
        link.setDisplayOrder(linkRepository.findByEntityTypeAndEntityIdAndPurposeOrderByDisplayOrderAscCreatedAtAsc(ENTITY_TYPE, documentId, PURPOSE).size());
        linkRepository.save(link);
        return stored;
    }

    private StoredFile activeFile(UUID id) {
        StoredFile file = fileRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "MANUALHUB_FILE_NOT_FOUND"));
        if (file.getDeletedAt() != null) throw new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "MANUALHUB_FILE_NOT_FOUND");
        return file;
    }

    private ManualHubDocument document(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
    }

    private void bumpCount(ManualHubDocument document) {
        document.setFileCount(document.getFileCount() + 1);
        document.setUpdatedAt(Instant.now());
    }

    private UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof AccountPrincipal account ? account.getId() : null;
    }

    private UploadFileResponse response(UUID documentId, UUID fileId) {
        return UploadFileResponse.builder().file(UploadFileResponse.FileInfo.builder().url(publicUrl(documentId, fileId)).build()).build();
    }

    private AppException fetchFailed() {
        return new AppException("Không tải được file từ OnlyOffice", HttpStatus.BAD_GATEWAY, "ONLYOFFICE_FETCH_FAILED");
    }
}
