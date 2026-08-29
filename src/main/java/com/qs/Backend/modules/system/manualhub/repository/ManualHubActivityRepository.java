package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualHubActivityRepository extends JpaRepository<ManualHubActivity, Long> {

    List<ManualHubActivity> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    List<ManualHubActivity> findAllByOrderByCreatedAtDesc();
}
