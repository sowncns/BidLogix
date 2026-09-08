package com.qs.Backend.platform.file.repository;

import com.qs.Backend.platform.file.entity.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileLinkRepository extends JpaRepository<FileLink, UUID> {
    List<FileLink> findByEntityTypeAndEntityIdAndPurposeOrderByDisplayOrderAscCreatedAtAsc(String entityType, UUID entityId, String purpose);
    Optional<FileLink> findByEntityTypeAndEntityIdAndFileId(String entityType, UUID entityId, UUID fileId);
    List<FileLink> findByEntityTypeAndEntityId(String entityType, UUID entityId);
    long countByFileId(UUID fileId);
}
