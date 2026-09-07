package com.qs.Backend.modules.crm.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String organizationId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String fullName;

    private String email;
    private String phone;

    private String companyName;
    private String source;
    private String statusId;
    private String customerType = "official";
    private String sourceCampaignId;
    private String notes;

    private String assignedSalesId;
    private String createdBy;
    private String mainPhone;
    private String mainEmail;
    private String website;
    private String address;

    private String gender;
    private String region;

    @ElementCollection
    private List<String> groupIds;

    @ElementCollection
    private List<String> businessFieldIds;

    private Instant lastContactAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant deletedAt; // null = chưa xóa, khác null = đã soft-delete
}
