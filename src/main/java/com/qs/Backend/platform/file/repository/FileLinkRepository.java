package com.qs.Backend.platform.file.repository;

import com.qs.Backend.platform.file.entity.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileLinkRepository extends JpaRepository<FileLink, String> {
    List<FileLink> findByEntityTypeAndEntityIdAndPurposeOrderByDisplayOrderAscCreatedAtAsc(String entityType, String entityId, String purpose);
    Optional<FileLink> findByEntityTypeAndEntityIdAndFileId(String entityType, String entityId, String fileId);
    long countByFileId(String fileId);
}
