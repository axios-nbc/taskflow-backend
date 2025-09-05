package org.example.taskflowd.domain.task.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskflowd.common.dto.response.ApiPageResponse;
import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.domain.task.dto.request.TaskCreateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskStatusUpdateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskUpdateRequest;
import org.example.taskflowd.domain.task.dto.response.*;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskResponseMessage;
import org.example.taskflowd.domain.task.service.TaskExternalService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    // In Domain
    private final TaskExternalService taskExternalService;

    /* ========== Main Method ========== */
    // <<< /tasks >>>
    // 2.1 Task 생성
    @PostMapping
    public ResponseEntity<ApiResponse<TaskCreateResponse>> createTask(
            @Validated @RequestBody TaskCreateRequest request,
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId) {
        return ApiResponse.created(
                TaskResponseMessage.TASK_CREATED,
                taskExternalService.createTask(request, loginUserId),
                null);
    }

    // 2.2 Task 목록 조회
    @GetMapping
    public ResponseEntity<ApiPageResponse<TaskListItemResponse>> getAllTasks(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") Long assigneeID
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.sort(Task.class).by(Task::getCreatedAt).descending());

        return ApiPageResponse.success(taskExternalService.getTasks(pageable));
    }

    // <<< /tasks/{taskId} >>>
    // 2.3 Task 목록 조회
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTaskByTaskId(@PathVariable Long taskId) {
        return ApiResponse.ok(taskExternalService.getDetailedTaskById(taskId));
    }

    // 2.4 Task 수정
    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskUpdateResponse>> updateTask(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @Validated @RequestBody TaskUpdateRequest request,
            @PathVariable Long taskId
    ) {
        return ApiResponse.ok(taskExternalService.updateTask(request, taskId, loginUserId));
    }

    // 2.6 Task 삭제
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Object>> deleteTask(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @PathVariable Long taskId
    ) {
        taskExternalService.deleteTask(taskId, loginUserId);
        return ApiResponse.ok(null);
    }


    // <<< /tasks/{taskId}/status >>>
    // 2.5 Task 상태 업데이트
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskStatusChangeResponse>> updateTaskStatus(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @Validated @RequestBody TaskStatusUpdateRequest request,
            @PathVariable Long taskId
    ) {
        return ApiResponse.ok(taskExternalService.updateTaskStatus(request, taskId, loginUserId));
    }
}
