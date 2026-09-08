package com.qs.Backend.platform.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// Logical link only (no FK to a business "organization" table, which doesn't exist yet).
@Entity
@Table(name = "account_organizations")
@Getter
@Setter
public class AccountOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
