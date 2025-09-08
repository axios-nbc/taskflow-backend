package org.example.taskflowd.domain.activityLog.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.taskflowd.common.annotation.ActivityLogger;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.activityLog.service.ActivityLogInternalService;
import org.example.taskflowd.domain.task.dto.response.TaskCreateResponse;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.service.TaskInternalService;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final ActivityLogInternalService activityLogInternalService;
    private final TaskInternalService taskInternalService;

    // 메서드에 @ActivityLogger를 붙일 경우 실행
    @Around(value = "@annotation(activityLogger)")
    public Object logActivity(ProceedingJoinPoint pjp, ActivityLogger activityLogger) throws Throwable {

        // 1. EnumType
        ActLogEnum logType = activityLogger.type();

        // 2. before process
        int paramIndex = activityLogger.paramIndex();
        Long taskId = (paramIndex >= 0) ? (Long) pjp.getArgs()[paramIndex] : -1;

        // 3. after process
        Object result;
        Task task;

        TaskStatus beforeStatus = getBeforeStatus(logType, taskId);

        switch (logType) {
            // DELETE 작업의 경우, getTask를 먼저 불러오고 proceed
            case TASK_DELETED, COMMENT_DELETED -> {
                task = getTask(logType, taskId, null);
                result = pjp.proceed();
            }
            // 그 외에 작업의 경우, proceed를 하고 getTask
            default -> {
                result = pjp.proceed();
                task = getTask(logType, taskId, result);
            }
        }

        saveActivityLog(logType, task, beforeStatus);
        return result;
    }

    private void saveActivityLog(ActLogEnum logType, Task task, TaskStatus beforeStatus) {

        String message = switch (logType) {
            // Task
            case TASK_CREATED -> String.format("새로운 작업 '%s'를 생성했습니다.", task.getTitle());
            case TASK_UPDATED -> "작업 정보를 수정했습니다.";
            case TASK_DELETED -> "작업을 삭제했습니다.";
            case TASK_STATUS_CHANGED -> String.format("작업 상태를 '%s'에서 '%s'로 변경했습니다.", beforeStatus.name(), task.getStatus().name());
            // Comment
            case COMMENT_CREATED -> "작업에 댓글을 작성했습니다.";
            case COMMENT_UPDATED -> "댓글을 수정했습니다.";
            case COMMENT_DELETED -> "댓글을 삭제했습니다.";
        };

        activityLogInternalService.saveActivityLog(logType, task, message);
    }

    private Task getTask(ActLogEnum type, Long taskId, Object result) throws Exception {

        if (taskId == -1) {
            // 만약 파라미터가 아닌 result에서 값을 가져와야 하는 경우
            taskId = switch (type) {
                case TASK_CREATED -> ((TaskCreateResponse) result).id();
                default -> throw new Exception();
            };
        }

        return taskInternalService.getTaskByIdOrThrow(taskId);
    }

    private TaskStatus getBeforeStatus(ActLogEnum type, Long taskId) {

        if (type != ActLogEnum.TASK_STATUS_CHANGED)
            return null;

        return taskInternalService.getTaskByIdOrThrow(taskId).getStatus();
    }
}