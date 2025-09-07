package org.example.taskflowd.domain.activityLog.service;

import org.example.taskflowd.domain.activityLog.dto.response.ActivityLogListResponse;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ActivityLogInternalService {

    void saveActivityLog(ActLogEnum type, Long userId, Long taskId, String description);

    ActivityLogListResponse getActivityLogs(Pageable pageable, ActLogEnum actLogEnum, Long userId, Long taskId,
                                            LocalDateTime startDate, LocalDateTime endDate);
}
