package com.qs.Backend.platform.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String originalName;
    private String storageKey;
    private String mimeType;
    private long sizeBytes;
    private String visibility;
    private String uploadedBy;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    private Instant deletedAt;
}
