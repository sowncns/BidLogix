package com.qs.Backend.modules.system.manualhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manualhub_document_versions")
@Getter
@Setter
public class ManualHubDocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private String version;

    private String title;

    private String format;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
