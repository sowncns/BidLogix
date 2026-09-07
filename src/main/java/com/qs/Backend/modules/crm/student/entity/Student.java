package com.qs.Backend.modules.crm.student.entity;

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
@Table(name = "students")
@Getter
@Setter
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "full_name", nullable = false)
    private String fullName;
    private String email;
    @Column(nullable = false)
    private String phone;
    @Column(name = "business_field")
    private String businessField;
    @Column(name = "deposit_agreed", nullable = false)
    private boolean depositAgreed;
    @Column(nullable = false)
    private String status = "registered";
    private String notes;
    @Column(name = "source_campaign_id")
    private String sourceCampaignId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    @Column(name = "deleted_at")
    private Instant deletedAt;
}
