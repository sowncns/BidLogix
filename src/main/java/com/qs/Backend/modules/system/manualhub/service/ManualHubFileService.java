package com.qs.Backend.modules.system.manualhub.service;

import com.qs.Backend.modules.system.manualhub.dto.UploadFileResponse;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubFile;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentRepository;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubFileRepository;
import com.qs.Backend.platform.file.FileStorageService;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

// This backend's own base URL for building publicly-fetchable file links.
// Defaults to the local dev server.port/context-path (see application.yml) —
// override via APP_PUBLIC_BASE_URL once this runs anywhere but localhost.
@Slf4j
@Service
@RequiredArgsConstructor
public class ManualHubFileService {

    @Value("${app.public-base-url:http://localhost:${server.port}${server.servlet.context-path:}}")
    private String publicBaseUrl;

    private final FileStorageService fileStorageService;
    private final ManualHubFileRepository fileRepository;
    private final ManualHubDocumentRepository documentRepository;

    @Transactional
    public UploadFileResponse upload(Long documentId, MultipartFile file) {

        ManualHubDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
        String storageKey = fileStorageService.storeFile(file, "manualhub/" + documentId);
        ManualHubFile record = saveFileRecord(documentId, storageKey, file.getOriginalFilename(), file.getContentType(), file.getSize());
        document.setFileCount(document.getFileCount() + 1);
        document.setUpdatedAt(Instant.now());
        return buildUploadResponse(documentId, record.getId());
    }

    /** Used by the OnlyOffice save callback, which hands us the edited
     *  document as a URL to re-download, not a MultipartFile. */
    @Transactional
    public ManualHubFile storeFromUrl(Long documentId, String sourceUrl, String originalFileName, String contentType) {
        byte[] bytes;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            bytes = response.body();
        } catch (IOException | InterruptedException e) {
           log.error(
                "Không tải được file từ OnlyOffice ONLYOFFICE_FETCH_FAILED"
            );
            throw new AppException("Không tải được file từ OnlyOffice", HttpStatus.BAD_GATEWAY, "ONLYOFFICE_FETCH_FAILED");
        }
        String storageKey = fileStorageService.storeBytes(bytes, originalFileName, "manualhub/" + documentId);
        ManualHubFile record = saveFileRecord(documentId, storageKey, originalFileName, contentType, (long) bytes.length);
        documentRepository.findById(documentId).ifPresent(document -> {
            document.setFileCount(document.getFileCount() + 1);
            document.setUpdatedAt(Instant.now());
        });
        return record;
    }

    /** Generates a blank .docx and stores it as the document's first file, so
     *  a freshly-created document already has a real, server-fetchable
     *  fileUrl and opens straight in OnlyOffice — instead of falling back to
     *  the client-side editor until the user's first manual save. */
    @Transactional
    public ManualHubFile createBlankDocx(Long documentId, String title) {
        byte[] bytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.createRun().setText(title != null ? title : "");
            doc.write(out);
            bytes = out.toByteArray();
        } catch (IOException e) {
            throw new AppException("Không tạo được file docx trống", HttpStatus.INTERNAL_SERVER_ERROR, "MANUALHUB_BLANK_DOCX_FAILED");
        }
        String fileName = (title != null && !title.isBlank() ? title : "document") + ".docx";
        String storageKey = fileStorageService.storeBytes(bytes, fileName,
                "manualhub/" + documentId);
        ManualHubFile record = saveFileRecord(documentId, storageKey, fileName,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", (long) bytes.length);
        documentRepository.findById(documentId).ifPresent(document -> {
            document.setFileCount(document.getFileCount() + 1);
            document.setUpdatedAt(Instant.now());
        });
        return record;
    }

    public Resource load(Long fileId) {
        ManualHubFile record = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "MANUALHUB_FILE_NOT_FOUND"));
        return fileStorageService.loadAsResource(record.getStorageKey());
    }

    public ManualHubFile findMeta(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "MANUALHUB_FILE_NOT_FOUND"));
    }

    public String publicUrl(Long documentId, Long fileId) {
        return publicBaseUrl + "/public/manualhub/documents/" + documentId + "/files/" + fileId;
    }

    private ManualHubFile saveFileRecord(Long documentId, String storageKey, String originalName, String contentType, Long size) {
        ManualHubFile record = new ManualHubFile();
        record.setDocumentId(documentId);
        record.setStorageKey(storageKey);
        record.setOriginalName(originalName);
        record.setContentType(contentType);
        record.setSize(size);
        record.setCreatedAt(Instant.now());
        return fileRepository.save(record);
    }

    private UploadFileResponse buildUploadResponse(Long documentId, Long fileId) {
        return UploadFileResponse.builder()
                .file(UploadFileResponse.FileInfo.builder().url(publicUrl(documentId, fileId)).build())
                .build();
    }
}
