package org.example.taskflowd.domain.activityLog.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.example.taskflowd.common.annotation.ActivityLogger;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.activityLog.service.ActivityLogInternalService;
import org.example.taskflowd.domain.task.dto.response.TaskCreateResponse;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final ActivityLogInternalService activityLogInternalService;

    @AfterReturning(value = "@annotation(activityLogger)", returning = "result")
    public void logActivity(ActivityLogger activityLogger, Object result) {

        ActLogEnum logType = activityLogger.type();

        TaskCreateResponse taskCreateResponse = (TaskCreateResponse) result;

        if (logType == ActLogEnum.TASK_CREATED) {
            activityLogInternalService.saveActivityLog(logType, taskCreateResponse.assigneeId(), taskCreateResponse.id(), "예제");
        }
    }
}