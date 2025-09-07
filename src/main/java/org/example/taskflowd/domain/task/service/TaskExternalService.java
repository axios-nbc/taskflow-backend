package org.example.taskflowd.domain.task.service;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.annotation.ActivityLogger;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.task.dto.request.TaskCreateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskStatusUpdateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskUpdateRequest;
import org.example.taskflowd.domain.task.dto.response.*;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.exception.TaskErrorCode;
import org.example.taskflowd.domain.task.mapper.TaskMapper;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskExternalService {
    private final TaskInternalService taskInternalService;

    private final UserService userService;

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    /* ========== Helper Method ========== */
    private Task getTaskOrThrow(Long id) {
        return taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new InvalidTaskException(TaskErrorCode.TSK_SEARCH_FAILED_INVALID_ID));
    }
    private <T> T getTaskMapped(Long id, Function<Task, T> mapper) {
        Task task = getTaskOrThrow(id);

        return mapper.apply(task);
    }

    /* ========== Main Method ========== */
    // 2.1 Task 생성
    @ActivityLogger(type = ActLogEnum.TASK_CREATED)
    @Transactional
    public TaskCreateResponse createTask(TaskCreateRequest request, Long loginUserId) {
        User writer = userService.getUser(loginUserId);
        User assignee = userService.getUser(request.assigneeId());

        Task task = taskMapper.toEntity(request, writer, assignee);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toCreateResponse(savedTask);
    }

    // 2.2 Task 목록 조회
    public Page<TaskListItemResponse> getTasks(Pageable pageable, Specification<Task> spec) {
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        return tasks.map(taskMapper::toListItemResponse);
    }

    // 2.3 Task 상세 조회
    public TaskDetailResponse getDetailedTaskById (Long taskId) {
        Task tasks = getTaskOrThrow(taskId);

        return getTaskMapped(taskId, taskMapper::toDetailResponse);
    }

    // 2.4 Task 수정
    @Transactional
    public TaskUpdateResponse updateTask(TaskUpdateRequest request, Long taskId, Long loginUserId) {
        // 현제 - 작성자가 및 담당자 아닐 경우 권한 부족
        User loginUser = userService.getUser(loginUserId);
        Task task = getTaskOrThrow(taskId);

        if (!(task.getAssignee().getId().equals(loginUser.getId()) ||
                task.getWriter().getId().equals(loginUser.getId()))) {
            throw new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN);
        }

        User newAssignee = task.getAssignee().getId().equals(request.assigneeId()) ?
                task.getAssignee() :
                userService.getUser(request.assigneeId());

        task.updateTask(
                request.title(),
                request.description(),
                request.dueDate(),
                newAssignee
        );


        task.updateStatus(request.status());
        task.updatePriority(request.priority());

        return taskMapper.toUpdateResponse(task);
    }

    // 2.5 Task 상태 업데이트
    @Transactional
    public TaskStatusChangeResponse updateTaskStatus(TaskStatusUpdateRequest request, Long taskId, Long loginUserId) {
        // 현제 - 작성자가 및 담당자 아닐 경우 권한 부족
        User loginUser = userService.getUser(loginUserId);
        Task task = getTaskOrThrow(taskId);

        if (!(task.getAssignee().getId().equals(loginUser.getId()) ||
                task.getWriter().getId().equals(loginUser.getId()))) {
            throw new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN);
        }

        task.updateStatus(request.status());

        return taskMapper.toStatusChangeResponse(task);
    }

    // 2.6 Task 삭제
    @Transactional
    public void deleteTask(Long taskId, Long loginUserId) {
        // 현제 - 작성자가 및 담당자 아닐 경우 권한 부족
        User loginUser = userService.getUser(loginUserId);
        Task task = getTaskOrThrow(taskId);

        if (!(task.getAssignee().getId().equals(loginUser.getId()) ||
                task.getWriter().getId().equals(loginUser.getId()))) {
            throw new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN);
        }

        task.delete();
    }
}
