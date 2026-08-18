package com.sown.BidLogix.core;

import com.sown.BidLogix.core.exception.AppException;
import com.sown.BidLogix.core.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/test-config")
@RequiredArgsConstructor
public class TestConfigController {

    private final JdbcTemplate jdbcTemplate;
    private final RedissonClient redissonClient;

    // 1. Test Database PostgreSQL
    @GetMapping("/db")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testDatabase() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        Map<String, Object> data = Map.of("db_status", "CONNECTED", "query_result", result != null ? result : 0);
        return ResponseEntity.ok(ApiResponse.ok(data, "PostgreSQL kết nối thành công!"));
    }

    // 2. Test Redis / Redisson
    @GetMapping("/redis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testRedis() {
        RBucket<String> bucket = redissonClient.getBucket("test:ping");
        bucket.set("pong", 60, TimeUnit.SECONDS);
        String val = bucket.get();
        Map<String, Object> data = Map.of("redis_status", "CONNECTED", "stored_value", val);
        return ResponseEntity.ok(ApiResponse.ok(data, "Redis / Redisson hoạt động tốt!"));
    }

    // 3. Test File Storage (Tạo file trực tiếp không qua MockMultipartFile)
    @GetMapping("/storage")
    public ResponseEntity<ApiResponse<String>> testStorage() {
        try {
            Path targetDir = Paths.get("uploads/test-folder");
            Files.createDirectories(targetDir);
            Path testFile = targetDir.resolve("test-" + UUID.randomUUID() + ".txt");
            Files.write(testFile, "BidLogix Storage Test Content".getBytes());

            return ResponseEntity.ok(ApiResponse.ok(testFile.toString(), "Lưu file vào thư mục thành công!"));
        } catch (Exception e) {
            throw new AppException("Không thể ghi file test: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_TEST_ERROR");
        }
    }

    // 4. Test Exception Handler
    @GetMapping("/exception")
    public ResponseEntity<Void> testException() {
        throw new AppException("Test lỗi nghiệp vụ thành công!", HttpStatus.BAD_REQUEST, "TEST_ERROR_CODE");
    }
}