package org.example.taskflowd.domain.activityLog.service;

import org.example.taskflowd.domain.activityLog.dto.response.ActivityLogListResponse;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ActivityLogInternalService {

    void saveActivityLog(ActLogEnum type, Task task, String description);

    ActivityLogListResponse getActivityLogs(Pageable pageable, ActLogEnum actLogEnum, Long userId, Long taskId,
                                            LocalDateTime startDate, LocalDateTime endDate);
}
