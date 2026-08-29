package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualHubFileRepository extends JpaRepository<ManualHubFile, Long> {

    List<ManualHubFile> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
