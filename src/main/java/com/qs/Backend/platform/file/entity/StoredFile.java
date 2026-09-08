package com.qs.Backend.platform.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@Setter
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String originalName;
    private String storageKey;
    private String mimeType;
    private long sizeBytes;
    private String visibility;
    private UUID uploadedBy;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    private Instant deletedAt;
}
