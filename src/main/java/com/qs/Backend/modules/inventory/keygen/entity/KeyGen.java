package com.qs.Backend.modules.inventory.keygen.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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
    private Long generatedBy;

    private String organizationId;

    private Long productItemId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
