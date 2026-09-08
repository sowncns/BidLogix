package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ManualHubActivityRepository extends JpaRepository<ManualHubActivity, UUID> {

    List<ManualHubActivity> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    List<ManualHubActivity> findAllByOrderByCreatedAtDesc();
}
