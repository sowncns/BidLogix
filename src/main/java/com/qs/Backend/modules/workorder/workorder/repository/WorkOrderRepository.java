package com.qs.Backend.modules.workorder.workorder.repository;

import com.qs.Backend.modules.workorder.workorder.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, String>, JpaSpecificationExecutor<WorkOrder> {
    long countByOrderNumberStartingWith(String prefix);
}
