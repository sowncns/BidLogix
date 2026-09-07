package com.qs.Backend.modules.crm.activity.repository;

import com.qs.Backend.modules.crm.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    Page<Activity> findByActivityableTypeAndActivityableId(String activityableType, String activityableId, Pageable pageable);
}
