package com.qs.Backend.modules.system.manualhub.service;

import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeCallbackRequest;
import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeConfigResponse;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubFile;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentRepository;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubFileRepository;
import com.qs.Backend.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnlyOfficeService {

    @Value("${app.public-base-url:http://localhost:${server.port}${server.servlet.context-path:}}")
    private String publicBaseUrl;

    // Statuses OnlyOffice sends where the document at `url` is the final,
    // ready-to-persist edit (2 = "ready for saving", 6 = "force-saved").
    // All other statuses (editing in progress, no changes, errors) are
    // acknowledged with error:0 but otherwise ignored.
    private static final List<Integer> SAVEABLE_STATUSES = List.of(2, 6);

    private final ManualHubDocumentRepository documentRepository;
    private final ManualHubFileRepository fileRepository;
    private final ManualHubFileService fileService;

    public OnlyOfficeConfigResponse buildConfig(Long documentId, boolean readOnly) {
        ManualHubDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
        List<ManualHubFile> files = fileRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
        if (files.isEmpty()) {
            throw new AppException("Tài liệu chưa có file để mở bằng OnlyOffice", HttpStatus.BAD_REQUEST, "MANUALHUB_NO_FILE");
        }
        ManualHubFile latest = files.get(0);
        String fileUrl = fileService.publicUrl(documentId, latest.getId()).replace("localhost", "host.docker.internal");
        String key = (documentId + "-" + document.getVersion() + "-" + System.currentTimeMillis());
        if (key.length() > 128) key = key.substring(0, 128);

        return OnlyOfficeConfigResponse.builder()
                .documentType("word")
                .document(OnlyOfficeConfigResponse.DocumentInfo.builder()
                        .fileType("docx")
                        .key(key)
                        .title(document.getTitle() + ".docx")
                        .url(fileUrl)
                        .build())
                .editorConfig(OnlyOfficeConfigResponse.EditorConfigInfo.builder()
                        .mode(readOnly ? "view" : "edit")
                        .callbackUrl(callbackUrl(documentId))
                        .lang("vi")
                        .build())
                .build();
    }

    // Not @Transactional here on purpose: storeFromUrl below has its own
    // @Transactional, and if that inner transaction throws, the enclosing one
    // gets marked rollback-only regardless of this method's try/catch — the
    // outer commit then fails with UnexpectedRollbackException even though
    // this method "handled" the error and returned normally. Leaving this
    // method non-transactional means only the inner call's own transaction
    // is affected by its own failure.
    public Map<String, Object> handleCallback(Long documentId, OnlyOfficeCallbackRequest body) {
        if (body.getStatus() == null || !SAVEABLE_STATUSES.contains(body.getStatus())) {
            return Map.of("error", 0);
        }
        if (body.getUrl() == null) {
            return Map.of("error", 0);
        }
        try {
            fileService.storeFromUrl(documentId, body.getUrl(), "document.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            return Map.of("error", 0);
        } catch (Exception e) {
            return Map.of("error", 1);
        }
    }

    private String callbackUrl(Long documentId) {
        return (publicBaseUrl + "/public/manualhub/documents/" + documentId + "/onlyoffice-callback")
                .replace("localhost", "host.docker.internal");
    }
}
