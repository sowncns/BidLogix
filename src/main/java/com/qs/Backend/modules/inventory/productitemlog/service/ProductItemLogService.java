package com.qs.Backend.modules.inventory.productitemlog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qs.Backend.modules.inventory.productitemlog.dto.ProductItemLogListResponse;
import com.qs.Backend.modules.inventory.productitemlog.dto.ProductItemLogResponse;
import com.qs.Backend.modules.inventory.productitemlog.entity.ProductItemLog;
import com.qs.Backend.modules.inventory.productitemlog.repository.ProductItemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductItemLogService {

    private final ProductItemLogRepository productItemLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(UUID productItemId, String eventType, UUID actorId, String source, UUID customerId, String metadata) {
        ProductItemLog log = new ProductItemLog();
        log.setProductItemId(productItemId);
        log.setEventType(eventType);
        log.setActorId(actorId);
        log.setSource(source == null || source.isBlank() ? "system" : source);
        log.setCustomerId(customerId);
        log.setMetadata(readMetadata(metadata));
        log.setOccurredAt(Instant.now());
        log.setCreatedAt(Instant.now());
        productItemLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public ProductItemLogListResponse list(UUID productItemId, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);
        Page<ProductItemLog> page = productItemLogRepository.findByProductItemIdOrderByOccurredAtDesc(
                productItemId, PageRequest.of(safeOffset / safeLimit, safeLimit));
        return ProductItemLogListResponse.builder()
                .items(page.getContent().stream().map(this::toResponse).toList())
                .limit(safeLimit)
                .offset(safeOffset)
                .total(page.getTotalElements())
                .build();
    }

    private ProductItemLogResponse toResponse(ProductItemLog log) {
        return ProductItemLogResponse.builder()
                .id(log.getId())
                .productItemId(log.getProductItemId())
                .eventType(log.getEventType())
                .actorId(log.getActorId())
                .actorRole(log.getActorRole())
                .source(log.getSource())
                .customerId(log.getCustomerId())
                .metadata(log.getMetadata())
                .occurredAt(log.getOccurredAt())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private Map<String, Object> readMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception ignored) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("raw", metadata);
            return fallback;
        }
    }
}
