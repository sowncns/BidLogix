package com.qs.Backend.platform.auth.repository;

import com.qs.Backend.platform.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByCode(String code);
}
