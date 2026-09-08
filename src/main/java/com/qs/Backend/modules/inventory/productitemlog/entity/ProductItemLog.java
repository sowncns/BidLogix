package com.qs.Backend.modules.inventory.productitemlog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "product_item_logs")
@Getter
@Setter
public class ProductItemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productItemId;

    @Column(nullable = false, length = 50)
    private String eventType;

    private Long actorId;
    private String actorRole;

    @Column(nullable = false, length = 50)
    private String source = "system";

    private String customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    @Column(nullable = false)
    private Instant occurredAt = Instant.now();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
