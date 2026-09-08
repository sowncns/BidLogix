package com.qs.Backend.modules.system.manualhub.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manualhub_documents")
@Getter
@Setter
public class ManualHubDocument {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "root_id")
    private UUID rootId;

    @Column(name = "document_group_id", nullable = false)
    private UUID documentGroupId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "document_type")
    private String documentType;

    private String format;

    @JdbcTypeCode(SqlTypes.JSON)
    private String content;

    @Column(nullable = false)
    private String status = "draft";

    @Column(nullable = false)
    private String language = "vi";

    @Column(nullable = false)
    private String version = "1";

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = true;

    @Column(name = "file_count", nullable = false)
    private int fileCount = 0;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
