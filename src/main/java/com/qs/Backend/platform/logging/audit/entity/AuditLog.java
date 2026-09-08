package com.qs.Backend.platform.logging.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "rbac_audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "action_type", nullable = false)
    private String action;

    @Column(name = "performed_by", nullable = false)
    private UUID actorId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Column(name = "target_role_id")
    private UUID targetRoleId;

    @Column(name = "target_permission_id")
    private UUID targetPermissionId;

    @Column(name = "target_organization_id")
    private UUID targetOrganizationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new LinkedHashMap<>();

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
