package com.qs.Backend.platform.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Long accountId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
}
