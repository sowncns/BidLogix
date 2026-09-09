package com.qs.Backend.platform.activationrequest.repository;

import com.qs.Backend.platform.activationrequest.entity.ActivationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ActivationRequestRepository extends JpaRepository<ActivationRequest, UUID>, JpaSpecificationExecutor<ActivationRequest> {
}
