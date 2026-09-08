package com.qs.Backend.modules.workorder.servicelog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "service_logs")
@Getter
@Setter
public class ServiceLog {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column(name = "product_item_id")
    private String productItemId;
    @Column(name = "work_order_id")
    private String workOrderId;
    @Column(name = "service_date", nullable = false)
    private Instant serviceDate = Instant.now();
    @Column(name = "service_type", nullable = false)
    private String serviceType;
    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription;
    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parts_replaced", columnDefinition = "jsonb", nullable = false)
    private List<PartReplaced> partsReplaced = new ArrayList<>();
    @Column(name = "technician_id")
    private String technicianId;
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;
    @Column(name = "source_comment_id")
    private String sourceCommentId;
    @Column(name = "event_type")
    private String eventType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> eventMetadata = new LinkedHashMap<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Getter
    @Setter
    public static class PartReplaced {
        private String partName;
        private int quantity;
        private String partCode;
    }
}
