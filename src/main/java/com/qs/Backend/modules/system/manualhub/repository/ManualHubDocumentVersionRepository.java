package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManualHubDocumentVersionRepository extends JpaRepository<ManualHubDocumentVersion, Long> {

    List<ManualHubDocumentVersion> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    Optional<ManualHubDocumentVersion> findByDocumentIdAndVersion(Long documentId, String version);
}
