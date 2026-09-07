package com.qs.Backend.platform.file.repository;

import com.qs.Backend.platform.file.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {
}
