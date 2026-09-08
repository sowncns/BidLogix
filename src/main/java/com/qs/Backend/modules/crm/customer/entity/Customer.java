package com.qs.Backend.modules.crm.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "customer_customer_groups", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "customer_group_id")
    private List<String> groupIds;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "customer_business_fields", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "business_field_id")
    private List<String> businessFieldIds;

    private Instant lastContactAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant deletedAt; // null = chưa xóa, khác null = đã soft-delete
}
