package com.qs.Backend.modules.workorder.workordercomment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "work_order_comments")
@Getter
@Setter
public class WorkOrderComment {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "work_order_id", nullable = false)
    private String workOrderId;
    @Column(name = "author_id", nullable = false)
    private String authorId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "is_internal", nullable = false)
    private boolean internal;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
