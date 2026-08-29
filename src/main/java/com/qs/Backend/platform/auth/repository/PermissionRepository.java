package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
