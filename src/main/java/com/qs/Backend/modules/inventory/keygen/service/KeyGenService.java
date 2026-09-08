package com.qs.Backend.modules.inventory.keygen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.Backend.modules.inventory.keygen.dto.*;
import com.qs.Backend.modules.inventory.keygen.entity.KeyGen;
import com.qs.Backend.modules.inventory.keygen.repository.KeyGenRepository;
import com.qs.Backend.modules.inventory.productitem.entity.ProductItem;
import com.qs.Backend.modules.inventory.productitem.repository.ProductItemRepository;
import com.qs.Backend.modules.inventory.productitemlog.service.ProductItemLogService;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeyGenService {

    private static final Map<String, String> DURATION_PRIVATE_KEYS = Map.of(
            "week", "MachineLock_1Week",
            "days_30", "MachineLock_30Days",
            "days_60", "MachineLock_60Days",
            "year_1", "MachineLock_1Years",
            "years_2", "MachineLock_2Years",
            "infinity", "MachineLock_Infinity"
    );

    private final KeyGenRepository keyGenRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductItemLogService productItemLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public GenerateFactoryResponse generateFactory(GenerateFactoryRequest request, Long userId) {
        requireUser(userId);
        validateTime(request);
        List<FactoryPasswordResponse> passwords = new ArrayList<>();
        Map<String, Object> output = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            int currentMinute = (request.getMinute() + i) % 60;
            int currentHour = request.getHour();
            if (request.getMinute() + i >= 60) {
                currentHour = (request.getHour() + 1) % 24;
            }
            String input = generateFactoryInput(currentMinute, currentHour, request.getDay(), request.getMonth(), request.getYear());
            FactoryPasswordResponse password = new FactoryPasswordResponse(input, generatePassword(input));
            passwords.add(password);
            output.put("t+" + i, password);
        }
        saveHistory("factory", Map.of(
                "minute", request.getMinute(),
                "hour", request.getHour(),
                "day", request.getDay(),
                "month", request.getMonth(),
                "year", request.getYear()
        ), output, userId, null, null);
        return new GenerateFactoryResponse(passwords);
    }

    @Transactional
    public GenerateActiveResponse generateActive(GenerateActiveRequest request, Long userId) {
        return new GenerateActiveResponse(generateActivePassword(request.getInput(), userId, null, null));
    }

    @Transactional
    public String generateActiveWithItemId(String input, Long userId, Long productItemId, String reason) {
        return generateActivePassword(input, userId, productItemId, reason);
    }

    @Transactional
    public GenerateMachineLockResponse generateMachineLock(GenerateMachineLockRequest request, Long userId) {
        requireUser(userId);
        String input = normalizeInput(request.getInput());
        String password = normalizeInput(request.getPassword());
        MachineLockKeysResponse keys = MachineLockKeysResponse.builder()
                .week(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("week"), password)))
                .days30(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("days_30"), password)))
                .days60(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("days_60"), password)))
                .year1(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("year_1"), password)))
                .years2(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("years_2"), password)))
                .infinity(generatePassword(generateMachineLockInput(input, DURATION_PRIVATE_KEYS.get("infinity"), password)))
                .build();
        Long productItemId = resolveProductItemId(input);
        saveHistory("machine_lock", Map.of("input", input, "password", password), Map.of(
                "week", keys.getWeek(),
                "days_30", keys.getDays30(),
                "days_60", keys.getDays60(),
                "year_1", keys.getYear1(),
                "years_2", keys.getYears2(),
                "infinity", keys.getInfinity()
        ), userId, null, productItemId);
        logKeygen(productItemId, "machine_lock", userId);
        return new GenerateMachineLockResponse(keys);
    }

    @Transactional(readOnly = true)
    public KeyGenHistoryListResponse listHistory(String keyType, Long generatedBy, String organizationId, Long productItemId,
                                                 Instant dateFrom, Instant dateTo, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        Page<KeyGen> page = keyGenRepository.findAll(spec(keyType, generatedBy, organizationId, productItemId, dateFrom, dateTo),
                PageRequest.of(safeOffset / safeLimit, safeLimit));
        return KeyGenHistoryListResponse.builder()
                .items(page.getContent().stream().map(this::toResponse).toList())
                .total(page.getTotalElements())
                .limit(safeLimit)
                .offset(safeOffset)
                .build();
    }

    private String generateActivePassword(String input, Long userId, Long knownProductItemId, String reason) {
        requireUser(userId);
        String normalized = normalizeInput(input);
        String password = generatePassword(normalized);
        Long productItemId = knownProductItemId != null ? knownProductItemId : resolveProductItemId(normalized);
        Map<String, Object> inputData = new LinkedHashMap<>();
        inputData.put("input", normalized);
        if (reason != null && !reason.isBlank()) {
            inputData.put("reason", reason);
        }
        saveHistory("active", inputData, Map.of("password", password), userId, null, productItemId);
        logKeygen(productItemId, "active", userId);
        return password;
    }

    private void saveHistory(String keyType, Map<String, Object> inputData, Map<String, Object> outputData,
                             Long generatedBy, String organizationId, Long productItemId) {
        KeyGen history = new KeyGen();
        history.setKeyType(keyType);
        history.setInputData(writeJson(inputData));
        history.setOutputData(writeJson(outputData));
        history.setGeneratedBy(generatedBy);
        history.setOrganizationId(organizationId);
        history.setProductItemId(productItemId);
        history.setCreatedAt(Instant.now());
        keyGenRepository.save(history);
    }

    public String generatePassword(String input) {
        long sum = 1469598103934665603L;
        long prime = 1099511628211L;
        byte[] bytes = input.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte raw : bytes) {
            long b = raw & 0xffL;
            sum ^= b;
            sum *= prime;
            sum ^= b << 8;
            sum = Long.rotateLeft(sum, 13);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sum = sum * 6364136223846793005L + 1;
            long digit = (sum >>> 32) % 10;
            sb.append((char) ('0' + digit));
        }
        return sb.toString();
    }

    private String generateFactoryInput(int minute, int hour, int day, int month, int year) {
        return minute + "/" + hour + "/" + day + "/" + month + "/" + year;
    }

    private String generateMachineLockInput(String input, String privateKey, String password) {
        return input + "-" + privateKey + "-" + password;
    }

    private Long resolveProductItemId(String input) {
        String code = extractDeviceCode(input);
        if (code.isBlank()) {
            return null;
        }
        return productItemRepository.findByCodeAndDeletedAtIsNull(code)
                .map(ProductItem::getId)
                .orElse(null);
    }

    private String extractDeviceCode(String input) {
        int idx = input.indexOf('-');
        return idx > 0 ? input.substring(0, idx) : input;
    }

    private void logKeygen(Long productItemId, String keyType, Long userId) {
        if (productItemId != null) {
            productItemLogService.log(productItemId, "keygen", userId, "direct", null, "{\"key_type\":\"" + keyType + "\"}");
        }
    }

    private Specification<KeyGen> spec(String keyType, Long generatedBy, String organizationId, Long productItemId,
                                       Instant dateFrom, Instant dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyType != null && !keyType.isBlank()) predicates.add(cb.equal(root.get("keyType"), keyType.trim()));
            if (generatedBy != null) predicates.add(cb.equal(root.get("generatedBy"), generatedBy));
            if (organizationId != null && !organizationId.isBlank()) predicates.add(cb.equal(root.get("organizationId"), organizationId.trim()));
            if (productItemId != null) predicates.add(cb.equal(root.get("productItemId"), productItemId));
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            if (dateTo != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private KeyGenHistoryResponse toResponse(KeyGen history) {
        ProductItem productItem = history.getProductItemId() == null ? null : productItemRepository.findById(history.getProductItemId()).orElse(null);
        return KeyGenHistoryResponse.builder()
                .id(history.getId())
                .keyType(history.getKeyType())
                .inputData(readJson(history.getInputData()))
                .outputData(readJson(history.getOutputData()))
                .generatedBy(history.getGeneratedBy())
                .generatedByName("")
                .organizationId(history.getOrganizationId())
                .productItemId(history.getProductItemId())
                .productItemCode(productItem == null ? null : productItem.getCode())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private String normalizeInput(String input) {
        String normalized = input == null ? "" : input.trim();
        if (normalized.isEmpty()) {
            throw new AppException("Input is required", HttpStatus.BAD_REQUEST, "KEYGEN_INVALID_INPUT");
        }
        return normalized;
    }

    private void validateTime(GenerateFactoryRequest request) {
        if (request.getMinute() < 0 || request.getMinute() > 59 || request.getHour() < 0 || request.getHour() > 23
                || request.getDay() < 1 || request.getDay() > 31 || request.getMonth() < 1 || request.getMonth() > 12
                || request.getYear() < 2000 || request.getYear() > 2100) {
            throw new AppException("Invalid time parameters", HttpStatus.BAD_REQUEST, "KEYGEN_INVALID_TIME_PARAMETERS");
        }
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new AppException("Cannot serialize keygen history", HttpStatus.INTERNAL_SERVER_ERROR, "KEYGEN_SERIALIZE_FAILED");
        }
    }

    private Map<String, Object> readJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
