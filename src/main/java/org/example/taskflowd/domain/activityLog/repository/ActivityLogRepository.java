package org.example.taskflowd.domain.activityLog.repository;

import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {
    Page<ActivityLog> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
