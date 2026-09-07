package com.qs.Backend.modules.crm.campaign.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "business_field_id")
    private String businessFieldId;

    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "assigned_person_id")
    private String assignedPersonId;

    @Column(name = "funnel_type", nullable = false)
    private String funnelType;

    @Column(name = "channel_type")
    private String channelType;

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
