package com.qs.Backend.platform.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_links")
@Getter
@Setter
public class FileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID fileId;
    private String entityType;
    private UUID entityId;
    private String purpose;
    private int displayOrder;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
