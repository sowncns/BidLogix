package com.qs.Backend.modules.crm.lead.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
public class Lead {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 50)
    private String phone;
    private String email;
    @Column(name = "business_field", columnDefinition = "TEXT")
    private String businessField;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> services = new ArrayList<>();
    @Column(name = "source_campaign_id")
    private UUID sourceCampaignId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
