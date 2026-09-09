package com.qs.Backend.modules.inventory.keygen.repository;

import com.qs.Backend.modules.inventory.keygen.entity.KeyGen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface KeyGenRepository extends JpaRepository<KeyGen, UUID>, JpaSpecificationExecutor<KeyGen> {
}
