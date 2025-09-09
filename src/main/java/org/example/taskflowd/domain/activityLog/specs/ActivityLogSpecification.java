package org.example.taskflowd.domain.activityLog.specs;

import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ActivityLogSpecification {

    public static Specification<ActivityLog> build(ActLogEnum type, Long userId, Long taskId, LocalDate startDate, LocalDate endDate) {

        return Specification.allOf(
                type != null ? equalType(type) : null,
                userId != null ? equalUser(userId) : null,
                taskId != null ? equalTask(taskId) : null,
                startDate != null ? fromStartDate(startDate.atStartOfDay()) : null,
                endDate != null ? untilEndDate(endDate.atTime(LocalTime.MAX)) : null
        );
    }

    public static Specification<ActivityLog> equalType(ActLogEnum type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<ActivityLog> equalUser(Long userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }

    public static Specification<ActivityLog> equalTask(Long taskId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("taskId"), taskId);
    }

    public static Specification<ActivityLog> fromStartDate(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<ActivityLog> untilEndDate(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }
}
