package com.qs.Backend.modules.system.manualhub.service;

import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeCallbackRequest;
import com.qs.Backend.modules.system.manualhub.dto.OnlyOfficeConfigResponse;
import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import com.qs.Backend.modules.system.manualhub.repository.ManualHubDocumentRepository;
import com.qs.Backend.platform.file.entity.StoredFile;
import com.qs.Backend.shared.exception.AppException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnlyOfficeService {

    @Value("${app.public-base-url:http://localhost:${server.port}${server.servlet.context-path:}}")
    private String publicBaseUrl;

    @Value("${app.onlyoffice.doc-server-url:}")
    private String onlyOfficeDocServerUrl;

    @Value("${app.onlyoffice.internal-url:${app.onlyoffice.doc-server-url:}}")
    private String onlyOfficeInternalUrl;

    @Value("${app.onlyoffice.jwt-secret:}")
    private String onlyOfficeJwtSecret;

    // Statuses OnlyOffice sends where the document at `url` is the final,
    // ready-to-persist edit (2 = "ready for saving", 6 = "force-saved").
    // All other statuses (editing in progress, no changes, errors) are
    // acknowledged with error:0 but otherwise ignored.
    private static final List<Integer> SAVEABLE_STATUSES = List.of(2, 6);

    private final ManualHubDocumentRepository documentRepository;
    private final ManualHubFileService fileService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public OnlyOfficeConfigResponse buildConfig(UUID documentId, boolean readOnly) {
        ManualHubDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
        List<StoredFile> files = fileService.list(documentId);
        if (files.isEmpty()) {
            throw new AppException("Tài liệu chưa có file để mở bằng OnlyOffice", HttpStatus.BAD_REQUEST, "MANUALHUB_NO_FILE");
        }
        StoredFile latest = files.get(files.size() - 1);
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
    public Map<String, Object> handleCallback(UUID documentId, OnlyOfficeCallbackRequest body) {
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

    public Map<String, String> forceSave(UUID documentId) {
        ManualHubDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
        String fileUrl = extractFileUrl(document.getContent());
        return Map.of("file_url", fileUrl == null ? "" : fileUrl);
    }

    public PdfDownload downloadPdf(UUID documentId, boolean requireCurrent, boolean attachment) {
        ManualHubDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND"));
        if (requireCurrent && (!"released".equals(document.getStatus()) || !document.isCurrent())) {
            throw new AppException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND, "MANUALHUB_DOCUMENT_NOT_FOUND");
        }
        List<StoredFile> linkedFiles = fileService.list(documentId);
        StoredFile source = linkedFiles.stream()
                .filter(file -> !"application/pdf".equalsIgnoreCase(file.getMimeType()))
                .reduce((first, second) -> second)
                .or(() -> linkedFiles.stream().reduce((first, second) -> second))
                .orElseThrow(() -> new AppException("Tài liệu chưa có file", HttpStatus.BAD_REQUEST, "MANUALHUB_NO_FILE"));
        String disposition = (attachment ? "attachment" : "inline") + "; filename=\"" + safeDownloadName(document.getTitle()) + ".pdf\"";
        String ext = extension(source.getOriginalName());
        if (ext.isBlank()) ext = document.getFormat() == null ? "docx" : document.getFormat().toLowerCase();
        if ("pdf".equals(ext)) {
            return new PdfDownload(fileService.load(documentId, source.getId()), disposition);
        }

        String sourceIdentity = fileService.publicUrl(documentId, source.getId());
        PdfCache cache = extractPdfCache(document.getContent());
        if (cache != null && sourceIdentity.equals(cache.sourceFileUrl())) {
            try {
                return new PdfDownload(fileService.load(documentId, UUID.fromString(cache.fileId())), disposition);
            } catch (RuntimeException ignored) {
                // Stale cache record: regenerate from current source below.
            }
        }

        byte[] pdf = convertToPdfBytes(fileService.publicUrl(documentId, source.getId()).replace("localhost", "host.docker.internal"), ext, "pdf-" + documentId + "-" + System.nanoTime());
        StoredFile cached = fileService.storeBytes(documentId, pdf, safeDownloadName(document.getTitle()) + ".pdf", "application/pdf");
        updatePdfCache(document, cached.getId(), sourceIdentity);
        return new PdfDownload(new ByteArrayResource(pdf), disposition);
    }

    private byte[] convertToPdfBytes(String fileUrl, String fileType, String key) {
        if (onlyOfficeInternalUrl == null || onlyOfficeInternalUrl.isBlank()) {
            throw new AppException("OnlyOffice chưa được cấu hình", HttpStatus.BAD_GATEWAY, "ONLYOFFICE_NOT_CONFIGURED");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("async", false);
        payload.put("filetype", fileType);
        payload.put("outputtype", "pdf");
        payload.put("key", key);
        payload.put("url", fileUrl);
        if (onlyOfficeJwtSecret != null && !onlyOfficeJwtSecret.isBlank()) {
            SecretKey secretKey = Keys.hmacShaKeyFor(onlyOfficeJwtSecret.getBytes(StandardCharsets.UTF_8));
            payload.put("token", Jwts.builder().claims(payload).signWith(secretKey).compact());
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(payload);
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            URI endpoint = URI.create(trimRight(onlyOfficeInternalUrl) + "/ConvertService.ashx");
            for (int i = 0; i < 60; i++) {
                HttpRequest request = HttpRequest.newBuilder(endpoint)
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();
                Map<?, ?> result = objectMapper.readValue(client.send(request, HttpResponse.BodyHandlers.ofString()).body(), Map.class);
                Number error = (Number) result.get("error");
                if (error != null && error.intValue() != 0) throw new IllegalStateException("OnlyOffice convert error " + error);
                if (Boolean.TRUE.equals(result.get("endConvert")) && result.get("fileUrl") != null) {
                    return fetchBytes(rewriteOnlyOfficeUrl(result.get("fileUrl").toString()));
                }
                Thread.sleep(1000);
            }
            throw new IllegalStateException("conversion did not finish in time");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new AppException("Không chuyển được tài liệu sang PDF", HttpStatus.BAD_GATEWAY, "ONLYOFFICE_CONVERT_FAILED");
        }
    }

    private byte[] fetchBytes(String url) throws Exception {
        HttpResponse<byte[]> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) throw new IllegalStateException("unexpected status " + response.statusCode());
        return response.body();
    }

    private void updatePdfCache(ManualHubDocument document, UUID fileId, String sourceIdentity) {
        try {
            Map<String, Object> content = document.getContent() == null || document.getContent().isBlank()
                    ? new HashMap<>()
                    : objectMapper.readValue(document.getContent(), HashMap.class);
            content.put("pdf_cache", Map.of("file_id", fileId.toString(), "source_file_url", sourceIdentity));
            document.setContent(objectMapper.writeValueAsString(content));
            documentRepository.save(document);
        } catch (Exception ignored) {
        }
    }

    private PdfCache extractPdfCache(String content) {
        try {
            if (content == null || content.isBlank()) return null;
            Map<?, ?> root = objectMapper.readValue(content, Map.class);
            Object raw = root.get("pdf_cache");
            if (!(raw instanceof Map<?, ?> cache)) return null;
            String fileId = String.valueOf(cache.get("file_id"));
            String sourceFileUrl = String.valueOf(cache.get("source_file_url"));
            if (fileId.isBlank() || sourceFileUrl.isBlank()) return null;
            return new PdfCache(fileId, sourceFileUrl);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractFileUrl(String content) {
        try {
            if (content == null || content.isBlank()) return "";
            Map<?, ?> root = objectMapper.readValue(content, Map.class);
            Object value = root.get("file_url");
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String rewriteOnlyOfficeUrl(String rawUrl) throws Exception {
        if (onlyOfficeInternalUrl == null || onlyOfficeInternalUrl.isBlank()) return rawUrl;
        URL raw = URI.create(rawUrl).toURL();
        URL internal = URI.create(onlyOfficeInternalUrl).toURL();
        return internal.getProtocol() + "://" + internal.getAuthority() + raw.getFile();
    }

    private static String safeDownloadName(String title) {
        String value = title == null || title.isBlank() ? "document" : title.trim();
        return value.replace("\"", "").replace("/", "-").replace("\\", "-").replace("\n", " ").replace("\r", " ");
    }

    private static String extension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1).toLowerCase() : "";
    }

    private static String trimRight(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String callbackUrl(UUID documentId) {
        return (publicBaseUrl + "/public/manualhub/documents/" + documentId + "/onlyoffice-callback")
                .replace("localhost", "host.docker.internal");
    }

    public record PdfDownload(Resource resource, String contentDisposition) {}

    private record PdfCache(String fileId, String sourceFileUrl) {}
}
