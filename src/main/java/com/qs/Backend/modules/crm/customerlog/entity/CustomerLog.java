package com.qs.Backend.modules.crm.customerlog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_logs")
@Getter
@Setter
public class CustomerLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    @Column(name = "actor_id")
    private String actorId;
    @Column(nullable = false)
    private String action;
    @Column(name = "target_type", nullable = false)
    private String targetType = "customer";
    @Column(name = "target_id")
    private String targetId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<FieldChange> changes = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Getter
    @Setter
    public static class FieldChange {
        private String field;
        private Object old;
        private Object newValue;
    }
}
