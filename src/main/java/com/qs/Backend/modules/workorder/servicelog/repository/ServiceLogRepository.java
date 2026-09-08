package com.qs.Backend.modules.workorder.servicelog.repository;

import com.qs.Backend.modules.workorder.servicelog.entity.ServiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceLogRepository extends JpaRepository<ServiceLog, String>, JpaSpecificationExecutor<ServiceLog> {
}
