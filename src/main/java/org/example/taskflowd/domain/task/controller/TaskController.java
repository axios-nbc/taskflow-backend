package org.example.taskflowd.domain.task.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskflowd.common.dto.response.ApiPageResponse;
import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.common.security.AuthUser;
import org.example.taskflowd.domain.task.dto.request.TaskCreateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskStatusUpdateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskUpdateRequest;
import org.example.taskflowd.domain.task.dto.response.*;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskResponseMessage;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.filter.TaskSpecs;
import org.example.taskflowd.domain.task.service.TaskExternalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
            @AuthenticationPrincipal AuthUser authUser,
            @Validated @RequestBody TaskCreateRequest request
    ) {
        TaskCreateResponse response = taskExternalService.createTask(request, authUser.id());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())   // TaskCreateResponse 안에 taskId 필드가 있다고 가정
                .toUri();

        return ApiResponse.created(
                TaskResponseMessage.TASK_CREATED,
                response,
                location);
    }

    // 2.2 Task 목록 조회
    @GetMapping
    public ResponseEntity<ApiPageResponse<TaskListItemResponse>> getAllTasks(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "assigneeId") Long assigneeId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.sort(Task.class).by(Task::getCreatedAt).descending());

        Specification<Task> spec = Specification.unrestricted();
        spec = spec.and(TaskSpecs.notDeleted());
        if (status != null)
            spec = spec.and(TaskSpecs.hasStatus(status));
        if (search != null && !search.isBlank())
            spec = spec.and(TaskSpecs.matchingSearch(search));
        if (assigneeId != null)
            spec = spec.and(TaskSpecs.assignedTo(assigneeId));


        Page<TaskListItemResponse> pageDto = taskExternalService.getTasks(pageable, spec);

        return ApiPageResponse.success(pageDto);
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
            @AuthenticationPrincipal AuthUser authUser,
            @Validated @RequestBody TaskUpdateRequest request,
            @PathVariable Long taskId
    ) {
        return ApiResponse.ok(taskExternalService.updateTask(request, taskId, authUser.id()));
    }

    // 2.6 Task 삭제
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Object>> deleteTask(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long taskId
    ) {
        taskExternalService.deleteTask(taskId, authUser.id());
        return ApiResponse.ok(null);
    }


    // <<< /tasks/{taskId}/status >>>
    // 2.5 Task 상태 업데이트
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskStatusChangeResponse>> updateTaskStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @Validated @RequestBody TaskStatusUpdateRequest request,
            @PathVariable Long taskId
    ) {
        return ApiResponse.ok(taskExternalService.updateTaskStatus(request, taskId, authUser.id()));
    }
}
