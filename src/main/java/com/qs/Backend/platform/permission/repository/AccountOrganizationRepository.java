package com.qs.Backend.platform.permission.repository;

import com.qs.Backend.platform.permission.entity.AccountOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountOrganizationRepository extends JpaRepository<AccountOrganization, Long> {
    List<AccountOrganization> findByAccountId(UUID accountId);

    void deleteByAccountId(UUID accountId);

    boolean existsByAccountIdAndOrganizationId(UUID accountId, UUID organizationId);
}
