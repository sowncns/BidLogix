package com.qs.Backend.modules.workorder.workordercomment.repository;

import com.qs.Backend.modules.workorder.workordercomment.entity.WorkOrderComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkOrderCommentRepository extends JpaRepository<WorkOrderComment, String> {
    Page<WorkOrderComment> findByWorkOrderIdAndDeletedAtIsNull(String workOrderId, Pageable pageable);
    Optional<WorkOrderComment> findByIdAndDeletedAtIsNull(String id);
}
