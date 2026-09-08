package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManualHubDocumentVersionRepository extends JpaRepository<ManualHubDocumentVersion, UUID> {

    List<ManualHubDocumentVersion> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    Optional<ManualHubDocumentVersion> findByDocumentIdAndVersion(UUID documentId, String version);
}
