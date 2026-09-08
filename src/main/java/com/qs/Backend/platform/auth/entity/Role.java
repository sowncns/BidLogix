package com.qs.Backend.platform.auth.entity;

import com.qs.Backend.platform.permission.entity.DataScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. ADMIN, ACCOUNTANT, CUSTOMER

    @Column(nullable = false)
    private String name; // e.g. ADMIN, ACCOUNTANT, WAREHOUSE_STAFF

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataScope dataScope = DataScope.SELF;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
