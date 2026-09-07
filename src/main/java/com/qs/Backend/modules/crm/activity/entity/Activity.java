package com.qs.Backend.modules.crm.activity.entity;

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
@Table(name = "activities")
@Getter
@Setter
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "activityable_type", nullable = false)
    private String activityableType;

    @Column(name = "activityable_id", nullable = false)
    private String activityableId;

    @Column(name = "sales_id", nullable = false)
    private String salesId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String content;

    @Column(name = "contact_at")
    private Instant contactAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
