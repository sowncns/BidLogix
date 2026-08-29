package com.qs.Backend.platform.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

// Auth identity only. `refId` is a logical reference to a business entity (e.g. customer, staff
// profile) - intentionally NOT a foreign key, so this module stays decoupled from business modules.
@Entity
@Table(name = "accounts")
@Getter
@Setter
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    private String phone;

    @Column(nullable = false)
    private String region = "VN";

    @Column(name = "ref_id")
    private Long refId;

    @Column(nullable = false)
    private boolean active = true;

    private Instant emailVerifiedAt;

    private Instant phoneVerifiedAt;

    @Column(nullable = false)
    private boolean emailNotifyActivationRequest = true;

    @Column(nullable = false)
    private boolean emailNotifyServiceRequest = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }
}
