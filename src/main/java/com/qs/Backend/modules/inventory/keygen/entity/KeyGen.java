package com.qs.Backend.modules.inventory.keygen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "keygen_history")
@Getter
@Setter
public class KeyGen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String keyType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inputData;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String outputData;

    @Column(nullable = false)
    private UUID generatedBy;

    private String organizationId;

    private UUID productItemId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
