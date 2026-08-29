package com.qs.Backend.platform.permission.repository;

import com.qs.Backend.platform.permission.entity.AccountOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOrganizationRepository extends JpaRepository<AccountOrganization, Long> {
    List<AccountOrganization> findByAccountId(Long accountId);
}
