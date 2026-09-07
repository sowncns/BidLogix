package com.qs.Backend.platform.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "file_links")
@Getter
@Setter
public class FileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String fileId;
    private String entityType;
    private String entityId;
    private String purpose;
    private int displayOrder;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
