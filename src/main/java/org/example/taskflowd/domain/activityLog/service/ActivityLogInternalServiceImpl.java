package org.example.taskflowd.domain.activityLog.service;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.domain.activityLog.dto.response.ActivityLogListResponse;
import org.example.taskflowd.domain.activityLog.dto.response.ActivityLogResponse;
import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.activityLog.repository.ActivityLogRepository;
import org.example.taskflowd.domain.activityLog.specs.ActivityLogSpecification;
import org.example.taskflowd.domain.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityLogInternalServiceImpl implements ActivityLogInternalService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void saveActivityLog(ActLogEnum type, Task task, String description) {

        ActivityLog activityLog = ActivityLog.create(type, task.getAssignee(), task, description);
        activityLogRepository.save(activityLog);
    }

    @Override
    public ActivityLogListResponse getActivityLogs(Pageable pageable, ActLogEnum type, Long userId, Long taskId,
                                                   LocalDateTime startDate, LocalDateTime endDate) {


        Specification<ActivityLog> spec = ActivityLogSpecification.build(type, userId, taskId, startDate, endDate);
        Page<ActivityLog> activityLogs = activityLogRepository.findAll(spec, pageable);

        return ActivityLogListResponse.of(activityLogs.map(ActivityLogResponse::from).toList());
    }
}