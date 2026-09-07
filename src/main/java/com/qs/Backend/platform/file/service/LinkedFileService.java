package com.qs.Backend.platform.file.service;

import com.qs.Backend.platform.file.FileStorageService;
import com.qs.Backend.platform.file.dto.LinkedFileResponse;
import com.qs.Backend.platform.file.entity.FileLink;
import com.qs.Backend.platform.file.entity.StoredFile;
import com.qs.Backend.platform.file.repository.FileLinkRepository;
import com.qs.Backend.platform.file.repository.StoredFileRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkedFileService {
    private final FileStorageService storageService;
    private final StoredFileRepository fileRepository;
    private final FileLinkRepository linkRepository;

    @Transactional
    public LinkedFileResponse upload(String entityType, String entityId, String purpose, MultipartFile file, String uploadedBy, int displayOrder) {
        if (file == null || file.isEmpty()) throw new AppException("File is required", HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
        if (uploadedBy == null || uploadedBy.isBlank()) throw new AppException("user not authenticated", HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        String storageKey = storageService.storeFile(file, entityType + "/" + entityId + "/" + purpose);

        StoredFile stored = new StoredFile();
        stored.setOriginalName(file.getOriginalFilename());
        stored.setStorageKey(storageKey);
        stored.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        stored.setSizeBytes(file.getSize());
        stored.setVisibility("internal");
        stored.setUploadedBy(uploadedBy);
        fileRepository.save(stored);

        FileLink link = new FileLink();
        link.setFileId(stored.getId());
        link.setEntityType(entityType);
        link.setEntityId(entityId);
        link.setPurpose(purpose);
        link.setDisplayOrder(displayOrder);
        linkRepository.save(link);
        return toResponse(stored, link);
    }

    @Transactional(readOnly = true)
    public List<LinkedFileResponse> list(String entityType, String entityId, String purpose) {
        return linkRepository.findByEntityTypeAndEntityIdAndPurposeOrderByDisplayOrderAscCreatedAtAsc(entityType, entityId, purpose).stream()
                .map(link -> toResponse(activeFile(link.getFileId()), link))
                .toList();
    }

    @Transactional(readOnly = true)
    public Download download(String entityType, String entityId, String fileId) {
        linkRepository.findByEntityTypeAndEntityIdAndFileId(entityType, entityId, fileId)
                .orElseThrow(() -> new AppException("file not found", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND"));
        StoredFile file = activeFile(fileId);
        return new Download(file.getOriginalName(), file.getMimeType(), storageService.loadAsResource(file.getStorageKey()));
    }

    @Transactional
    public void delete(String entityType, String entityId, String fileId) {
        FileLink link = linkRepository.findByEntityTypeAndEntityIdAndFileId(entityType, entityId, fileId)
                .orElseThrow(() -> new AppException("file not found", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND"));
        linkRepository.delete(link);
        if (linkRepository.countByFileId(fileId) == 0) {
            StoredFile file = activeFile(fileId);
            file.setDeletedAt(Instant.now());
        }
    }

    private StoredFile activeFile(String fileId) {
        StoredFile file = fileRepository.findById(fileId).orElseThrow(() -> new AppException("file not found", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND"));
        if (file.getDeletedAt() != null) throw new AppException("file not found", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND");
        return file;
    }

    private LinkedFileResponse toResponse(StoredFile file, FileLink link) {
        return LinkedFileResponse.builder().id(file.getId()).originalName(file.getOriginalName()).mimeType(file.getMimeType()).sizeBytes(file.getSizeBytes()).visibility(file.getVisibility()).displayOrder(link.getDisplayOrder()).createdAt(file.getCreatedAt()).build();
    }

    public record Download(String fileName, String mimeType, Resource resource) {}
}
