package com.qs.Backend.platform.permission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_organizations")
@Getter
@Setter
@IdClass(AccountOrganization.Key.class)
public class AccountOrganization {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID accountId;

    @Id
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    public static class Key implements Serializable {
        private UUID accountId;
        private UUID organizationId;

        public Key() {
        }

        public Key(UUID accountId, UUID organizationId) {
            this.accountId = accountId;
            this.organizationId = organizationId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(accountId, key.accountId) && Objects.equals(organizationId, key.organizationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, organizationId);
        }
    }
}
