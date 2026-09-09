package com.qs.Backend.modules.inventory.keygen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "keygen_history")
@Getter
@Setter
public class KeyGen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String keyType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String inputData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String outputData;

    @Column(nullable = false)
    private UUID generatedBy;

    private UUID organizationId;

    private UUID productItemId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
