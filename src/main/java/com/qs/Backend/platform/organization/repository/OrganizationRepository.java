package com.qs.Backend.platform.organization.repository;

import com.qs.Backend.platform.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);

    List<Organization> findByParentId(UUID parentId);
    Page<Organization> findByStatus(String status, Pageable pageable);
}
