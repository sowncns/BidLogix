package com.qs.Backend.modules.crm.campaign.repository;

import com.qs.Backend.modules.crm.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, String>, JpaSpecificationExecutor<Campaign> {
    Optional<Campaign> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);
}
