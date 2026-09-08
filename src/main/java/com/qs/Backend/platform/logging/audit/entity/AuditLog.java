package com.qs.Backend.platform.logging.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Actor fields are denormalized (no FK to accounts) so this stays readable even after the
// account is deleted/renamed, and so platform.logging doesn't depend on platform.auth.
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_username")
    private String actorUsername;

    // e.g. CREATE, UPDATE, DELETE, LOGIN, LOGIN_FAILED
    @Column(nullable = false)
    private String action;

    // e.g. Customer, WorkOrder, Role
    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
