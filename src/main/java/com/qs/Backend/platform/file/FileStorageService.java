package com.qs.Backend.platform.file;
import com.qs.Backend.shared.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.storage.upload-dir:/app/storage/uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Không thể khởi tạo thư mục lưu trữ: {}", uploadDir, e);
            throw new AppException("Khởi tạo bộ nhớ lưu trữ file thất bại", HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_INIT_ERROR");
        }
    }

    public String storeFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File tải lên không được rỗng!", HttpStatus.BAD_REQUEST, "INVALID_FILE");
        }

        try {
            Path targetDir = this.rootLocation.resolve(subFolder).normalize();
            Files.createDirectories(targetDir);

            String originalFileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String extension = originalFileName.contains(".") ? originalFileName.substring(originalFileName.lastIndexOf('.')) : "";
            String storedFileName = UUID.randomUUID() + extension;

            Path destinationFile = targetDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return subFolder + "/" + storedFileName;
        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ file vào đĩa: ", e);
            throw new AppException("Lưu file thất bại", HttpStatus.INTERNAL_SERVER_ERROR, "FILE_WRITE_ERROR");
        }
    }

    /** Stores raw bytes (e.g. re-fetched from an external URL) the same way
     *  {@link #storeFile} stores a MultipartFile — used by the OnlyOffice
     *  save callback, which hands us bytes, not a multipart upload. */
    public String storeBytes(byte[] bytes, String originalFileName, String subFolder) {
        try {
            Path targetDir = this.rootLocation.resolve(subFolder).normalize();
            Files.createDirectories(targetDir);

            String extension = originalFileName != null && originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                    : "";
            String storedFileName = UUID.randomUUID() + extension;

            Path destinationFile = targetDir.resolve(storedFileName);
            Files.write(destinationFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return subFolder + "/" + storedFileName;
        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ file vào đĩa: ", e);
            throw new AppException("Lưu file thất bại", HttpStatus.INTERNAL_SERVER_ERROR, "FILE_WRITE_ERROR");
        }
    }

    public Resource loadAsResource(String storageKey) {
        try {
            Path file = this.rootLocation.resolve(storageKey).normalize();
            if (!file.startsWith(this.rootLocation)) {
                throw new AppException("Đường dẫn file không hợp lệ", HttpStatus.BAD_REQUEST, "INVALID_STORAGE_KEY");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new AppException("Không tìm thấy file", HttpStatus.NOT_FOUND, "FILE_NOT_FOUND");
        }
    }
}