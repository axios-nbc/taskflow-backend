package org.example.taskflowd.domain.activityLog.specs;

import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> build(ActLogEnum type, Long userId, Long taskId, LocalDateTime startDate, LocalDateTime endDate) {

        Specification<ActivityLog> spec = Specification.unrestricted();

        if (type != null) spec.and(equalType(type));
        if (userId != null) spec.and(equalUser(userId));
        if (taskId != null) spec.and(equalTask(taskId));
        if (startDate != null) spec.and(equalStartDate(startDate));
        if (endDate != null) spec.and(equalEndDate(endDate));

        return spec;
    }

    public static Specification<ActivityLog> equalType(ActLogEnum type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<ActivityLog> equalUser(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user_id"), userId);
    }

    public static Specification<ActivityLog> equalTask(Long taskId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("task_id"), taskId);
    }

    public static Specification<ActivityLog> equalStartDate(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("start_date"), startDate);
    }

    public static Specification<ActivityLog> equalEndDate(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("end_date"), endDate);
    }
}
