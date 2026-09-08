package com.qs.Backend.modules.workorder.workorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
public class WorkOrder {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;
    @Column(name = "product_item_id")
    private String productItemId;
    @Column(nullable = false, length = 50)
    private String type;
    @Column(nullable = false, length = 20)
    private String priority = "normal";
    @Column(name = "assigned_to")
    private String assignedTo;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, length = 50)
    private String status = "pending";
    @Column(name = "scheduled_date")
    private Instant scheduledDate;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
