package org.example.taskflowd.domain.task.service;

import org.example.taskflowd.domain.task.dto.request.TaskCreateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskStatusUpdateRequest;
import org.example.taskflowd.domain.task.dto.request.TaskUpdateRequest;
import org.example.taskflowd.domain.task.dto.response.*;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.exception.TaskErrorCode;
import org.example.taskflowd.domain.task.mapper.TaskMapper;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.task.service.TaskExternalService;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.example.taskflowd.domain.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TaskExternalServiceTest {
    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock TaskMapper taskMapper;
    @Mock UserServiceImpl userService;

    @Spy @InjectMocks TaskInternalServiceImpl taskInternalService;
    @InjectMocks TaskExternalService taskExternalService;

    @Captor ArgumentCaptor<Task> taskCaptor;
    @Captor ArgumentCaptor<Specification<Task>> specCaptor;
    @Captor ArgumentCaptor<Pageable> pageableCaptor;

    private User writer;
    private User assignee;
    private User another;
    private Task task;

    /* ========== Helper Method ========== */
    Task buildTask(String title, String description, User writer, User assignee) {
        return Task.builder()
                .title(title)
                .description(description)
                .writer(writer)
                .assignee(assignee)
                .status(null)
                .priority(null)
                .dueDate(null)
                .build();
    }
    Task buildTask(String title, String description, User writer, User assignee, TaskStatus status, TaskPriority priority, LocalDateTime dueDate) {
        return Task.builder()
                .title(title)
                .description(description)
                .writer(writer)
                .assignee(assignee)
                .status(status)
                .priority(priority)
                .dueDate(dueDate)
                .build();
    }

    @BeforeEach
    void setUp() {
        writer = new User(
                "writer", "writer123", "a@example.com", "123456");
        ReflectionTestUtils.setField(writer, "id", 10L);
        assignee = new User("assignee", "assignee123", "b@example.com", "55AA55");
        ReflectionTestUtils.setField(assignee, "id", 20L);
        another = new User("another", "another123", "c@example.com", "55AA55");
        ReflectionTestUtils.setField(another, "id", 30L);

        task = buildTask("작업", "내용", writer, assignee, TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now());
        ReflectionTestUtils.setField(task, "id", 100L);
    }

    @Test
    @DisplayName("createTask: 정상적인 request dto 입력시 response dto 반환")
    void createTask_shouldReturnTaskCreateResponseDto_whenGiveValidTaskCreateRequest() {
        // when
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest("제목","내용", now, TaskPriority.LOW, assignee.getId());

        given(userService.getUser(writer.getId())).willReturn(writer);
        given(userService.getUser(assignee.getId())).willReturn(assignee);

        Task entity = Task.builder()
                .title("제목").description("내용")
                .writer(writer).assignee(assignee)
                .priority(TaskPriority.LOW).status(TaskStatus.TODO)
                .dueDate(now).build();

        given(taskMapper.toEntity(any(TaskCreateRequest.class), eq(writer), eq(assignee)))
                .willReturn(entity);

        given(taskRepository.save(any(Task.class))).willAnswer(inv -> {
            Task t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 123L);
            return t;
        });

        UserResponseDto assigneeDto = mock(UserResponseDto.class);
        given(taskMapper.toCreateResponse(any(Task.class))).willAnswer(inv -> {
            Task t = inv.getArgument(0);
            return TaskCreateResponse.toDto(
                    t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(),
                    t.getPriority().getCode(), t.getStatus().getCode(),
                    t.getAssignee().getId(), assigneeDto, t.getCreatedAt(), t.getUpdatedAt()
            );
        });

        // when
        TaskCreateResponse taskCreateResponse = taskExternalService.createTask(taskCreateRequest, writer.getId());

        // then
        assertThat(taskCreateResponse.id()).isEqualTo(123L);
        assertThat(taskCreateResponse.title()).isEqualTo("제목");
        assertThat(taskCreateResponse.description()).isEqualTo("내용");
        assertThat(taskCreateResponse.priority()).isEqualTo(TaskPriority.LOW.getCode());
        assertThat(taskCreateResponse.status()).isEqualTo(TaskStatus.TODO.getCode());
        assertThat(taskCreateResponse.assigneeId()).isEqualTo(assignee.getId());
        assertThat(taskCreateResponse.assignee()).isSameAs(assigneeDto);

        then(taskRepository).should().save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();

        assertThat(savedTask.getWriter().getId()).isEqualTo(writer.getId());
        assertThat(savedTask.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    @DisplayName("createTask: 비정상적인 assignee 지정 시 오류 반환")
    void createTask_shouldThrow_whenGiveInvalidAssignee() {
        // given
        LocalDateTime now = LocalDateTime.now();
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest("제목","내용", now, TaskPriority.LOW, assignee.getId());
        given(userService.getUser(writer.getId())).willReturn(writer);
        given(userService.getUser(assignee.getId()))
                .willThrow(new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_ASSIGNEE));

        // when / then
        assertThatThrownBy(() -> taskExternalService.createTask(taskCreateRequest, writer.getId()))
                .isInstanceOf(InvalidTaskException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_ASSIGNEE);

        then(userService).should().getUser(writer.getId());
        then(userService).should().getUser(assignee.getId());
        then(taskMapper).shouldHaveNoInteractions();
        then(taskRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("createTask: 비정상적인 request dto 전달 시(dueDate) 예외 반환")
    void createTask_shouldThrow_whenMapperFails() {
        // given
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        TaskCreateRequest request = new TaskCreateRequest(
                "제목", "내용", past, TaskPriority.LOW, assignee.getId());

        given(userService.getUser(writer.getId())).willReturn(writer);
        given(userService.getUser(assignee.getId())).willReturn(assignee);

        given(taskMapper.toEntity(any(TaskCreateRequest.class), eq(writer), eq(assignee)))
                .willThrow(new IllegalArgumentException());

        // when / then
        assertThatThrownBy(() -> taskExternalService.createTask(request, writer.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        then(taskRepository).shouldHaveNoInteractions();
        then(taskMapper).should(times(1)).toEntity(any(TaskCreateRequest.class), eq(writer), eq(assignee));
        then(taskMapper).shouldHaveNoMoreInteractions();

        then(userService).should(times(1)).getUser(writer.getId());
        then(userService).should(times(1)).getUser(assignee.getId());
        then(userService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("createTask: request  null일 시 오류 반환")
    void createTask_shouldThrow_whenGiveNothing() {
        assertThatThrownBy(() -> taskExternalService.createTask(null, writer.getId()))
                .isInstanceOf(NullPointerException.class);

        then(taskMapper).shouldHaveNoInteractions();
        then(taskRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("getTasks: TaskPageResponse 반환")
    void getTasks_shouldReturnPage_whenGivePageable() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        Task t1 = buildTask("T1", "D1", writer, assignee, TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(t1, "id", 1L);
        Task t2 = buildTask("T2", "D2", writer, assignee, TaskStatus.IN_PROGRESS, TaskPriority.LOW, LocalDateTime.now().plusDays(2));
        ReflectionTestUtils.setField(t2, "id", 2L);

        List<Task> tasks = List.of(t1, t2);
        Page<Task> page = new PageImpl<>(tasks, pageable, tasks.size());

        given(taskRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        TaskListItemResponse r1 = mock(TaskListItemResponse.class);
        TaskListItemResponse r2 = mock(TaskListItemResponse.class);

        given(taskMapper.toListItemResponse(t1)).willReturn(r1);
        given(taskMapper.toListItemResponse(t2)).willReturn(r2);

        // when
        TaskPageResponse response = taskExternalService.getTasks(pageable, spec);

        then(taskRepository).should().findAll(specCaptor.capture(), pageableCaptor.capture());
        assertThat(specCaptor.getValue()).isNotNull();
        assertThat(pageableCaptor.getValue()).isEqualTo(pageable);

        then(taskMapper).should(times(1)).toListItemResponse(t1);
        then(taskMapper).should(times(1)).toListItemResponse(t2);
        then(taskMapper).shouldHaveNoMoreInteractions();

        List<TaskListItemResponse> contents = response.content();
        org.assertj.core.api.Assertions.assertThat(contents).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("getTasks: 결과가 없으면 빈 리스트 반환")
    void getTasks_shouldReturnEmpty_whenNoTasks() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        Page<Task> emptyPage = Page.empty(pageable);
        given(taskRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

        // when
        TaskPageResponse response = taskExternalService.getTasks(pageable, spec);

        // then
        then(taskMapper).shouldHaveNoInteractions();

        // 내용 비어 있음 검증
        List<TaskListItemResponse> actual = response.content(); // 또는 content()
        org.assertj.core.api.Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("getDetailedTaskById: 유효한 ID면 Task를 매핑해 상세 DTO 반환")
    void getDetailedTaskById_shouldReturnTaskDetailResponse_whenTaskExist() {
        // given
        Long taskId = 100L;
        doReturn(task).when(taskInternalService).getTaskByIdOrThrow(100L);

        UserResponseDto assigneeDto = mock(UserResponseDto.class);
        TaskDetailResponse detail = TaskDetailResponse.toDto(
                taskId,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority().name(),
                task.getStatus().name(),
                null,
                assigneeDto,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
        given(taskMapper.toDetailResponse(task)).willReturn(detail);

        // when
        Task getTask = taskInternalService.getTaskByIdOrThrow(taskId);
        TaskDetailResponse res = taskMapper.toDetailResponse(getTask);

        // then
        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo(taskId);
        assertThat(res.title()).isEqualTo("작업");
        assertThat(res.description()).isEqualTo("내용");
        assertThat(res.dueDate()).isEqualTo(task.getDueDate());
        assertThat(res.priority()).isEqualTo("MEDIUM");
        assertThat(res.status()).isEqualTo("TODO");
        assertThat(res.assignee()).isSameAs(assigneeDto);

        then(taskInternalService).should().getTaskByIdOrThrow(taskId);
        then(taskMapper).should().toDetailResponse(task);
        then(taskMapper).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("getDetailedTaskById: 유효하지 않은 ID면 예외 Throw")
    void getDetailedTaskById_shouldThrow_whenTaskDoesntExist() {
        Long taskId = 101L;

        // given
        doThrow(new InvalidTaskException(TaskErrorCode.TSK_SEARCH_FAILED_INVALID_ID))
                .when(taskInternalService).getTaskByIdOrThrow(taskId);

        // when / then
        assertThatThrownBy(() -> taskInternalService.getTaskByIdOrThrow(taskId))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessage(TaskErrorCode.TSK_SEARCH_FAILED_INVALID_ID.getMessage());

        then(taskMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("updateTask: 담장자 유지")
    void updateTask_shouldSuccess_whenKeepAssignee() {
        // given
        Long taskId = 100L;

        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        given(userService.getUser(writer.getId())).willReturn(writer);

        LocalDateTime newDue = LocalDateTime.now().plusDays(3);
        TaskUpdateRequest request = new TaskUpdateRequest(
                "새 제목", "새 내용", newDue, TaskPriority.HIGH, TaskStatus.DONE, assignee.getId());

        TaskUpdateResponse response = Mockito.mock(TaskUpdateResponse.class);
        given(taskMapper.toUpdateResponse(any(Task.class))).willReturn(response);

        // when
        TaskUpdateResponse res = taskExternalService.updateTask(request, taskId, writer.getId());

        // then
        assertThat(res).isSameAs(response);
        then(taskMapper).should().toUpdateResponse(taskCaptor.capture());
        Task mutated = taskCaptor.getValue();

        assertThat(mutated.getTitle()).isEqualTo("새 제목");
        assertThat(mutated.getDescription()).isEqualTo("새 내용");
        assertThat(mutated.getDueDate()).isEqualTo(newDue);
        assertThat(mutated.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(mutated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(mutated.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    @DisplayName("updateTask: 담장자 유지")
    void updateTask_shouldSuccess_whenChangeAssignee() {
        // given
        Long taskId = 100L;

        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        given(userService.getUser(writer.getId())).willReturn(writer);
        given(userService.getUser(another.getId())).willReturn(another);

        LocalDateTime newDue = LocalDateTime.now().plusDays(3);
        TaskUpdateRequest request = new TaskUpdateRequest(
                "새 제목", "새 내용", newDue, TaskPriority.HIGH, TaskStatus.DONE, another.getId());

        TaskUpdateResponse response = Mockito.mock(TaskUpdateResponse.class);
        given(taskMapper.toUpdateResponse(any(Task.class))).willReturn(response);

        // when
        TaskUpdateResponse res = taskExternalService.updateTask(request, taskId, writer.getId());

        // then
        assertThat(res).isSameAs(response);
        then(taskMapper).should().toUpdateResponse(taskCaptor.capture());
        Task mutated = taskCaptor.getValue();

        assertThat(mutated.getTitle()).isEqualTo("새 제목");
        assertThat(mutated.getDescription()).isEqualTo("새 내용");
        assertThat(mutated.getDueDate()).isEqualTo(newDue);
        assertThat(mutated.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(mutated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(mutated.getAssignee().getId()).isEqualTo(another.getId());
    }

    @Test
    @DisplayName("updateTask: 권한없을 경우 Forbidden")
    void updateTask_shouldThrow_whenForbidden() {
        // given
        Long taskId = 100L;

        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        given(userService.getUser(another.getId())).willReturn(another);

        LocalDateTime newDue = LocalDateTime.now().plusDays(3);
        TaskUpdateRequest request = new TaskUpdateRequest(
                "새 제목", "새 내용", newDue, TaskPriority.HIGH, TaskStatus.DONE, assignee.getId());

        // when / then
        assertThatThrownBy(() -> taskExternalService.updateTask(request, taskId, another.getId()))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessage(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN.getMessage());

        then(taskMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("updateTask: 존재하지 않는 assignee일 시 throw")
    void updateTask_shouldThrow_whenGiveInvalidAssignee() {
        // given
        Long taskId = 100L;
        Long invalidAssigneeId = 1000L;

        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        given(userService.getUser(writer.getId())).willReturn(writer);
        given(userService.getUser(invalidAssigneeId))
                .willThrow(new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_ASSIGNEE));

        TaskUpdateRequest request = new TaskUpdateRequest(
                "새 제목", "새 내용", LocalDateTime.now(), TaskPriority.HIGH, TaskStatus.DONE, invalidAssigneeId);

        // when / then
        assertThatThrownBy(() -> taskExternalService.updateTask(request, taskId, writer.getId()))
                .isInstanceOf(InvalidTaskException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_ASSIGNEE);

        then(taskMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("updateTaskStatus: 작성자 권한 변경")
    void updateTaskStatus_shouldSuccess_whenWriter() {
        // given
        Long taskId = 100L;
        given(userService.getUser(writer.getId())).willReturn(writer); 
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.DONE);
        TaskStatusChangeResponse response = Mockito.mock(TaskStatusChangeResponse.class);
        given(taskMapper.toStatusChangeResponse(any(Task.class))).willReturn(response);

        // when
        TaskStatusChangeResponse res = taskExternalService.updateTaskStatus(request, taskId, writer.getId());

        // then
        assertThat(res).isSameAs(response);

        then(taskMapper).should().toStatusChangeResponse(taskCaptor.capture());
        Task mutated = taskCaptor.getValue();
        assertThat(mutated.getStatus()).isEqualTo(TaskStatus.DONE);
    }
    @Test
    @DisplayName("updateTaskStatus: 담당자 권한 변경")
    void updateTaskStatus_shouldSuccess_whenAssignee() {
        // given
        Long taskId = 100L;
        given(userService.getUser(assignee.getId())).willReturn(assignee);
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.DONE);
        TaskStatusChangeResponse response = Mockito.mock(TaskStatusChangeResponse.class);
        given(taskMapper.toStatusChangeResponse(any(Task.class))).willReturn(response);

        // when
        TaskStatusChangeResponse res = taskExternalService.updateTaskStatus(request, taskId, assignee.getId());

        // then
        assertThat(res).isSameAs(response);

        then(taskMapper).should().toStatusChangeResponse(taskCaptor.capture());
        Task mutated = taskCaptor.getValue();
        assertThat(mutated.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("updateTaskStatus: 권환 없으면 Forbidden throw")
    void updateTaskStatus_shouldThrow_whenAnother() {
        // given
        Long taskId = 100L;
        given(userService.getUser(another.getId())).willReturn(another);
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        // when / then
        assertThatThrownBy(() -> taskExternalService.updateTaskStatus(request, taskId, another.getId()))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessage(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN.getMessage());

        then(taskMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("deleteTask: 작성자 작업 삭제")
    void deleteTask_shouldSuccess_whenWriter() {
        // given
        Long taskId = 100L;
        given(userService.getUser(writer.getId())).willReturn(writer);
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));

        // when
        taskExternalService.deleteTask(taskId, writer.getId());

        // then
        assertThat(task.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteTask: 담당자 작업 삭제")
    void deleteTask_shouldSuccess_whenAssignee() {
        // given
        Long taskId = 100L;
        given(userService.getUser(assignee.getId())).willReturn(assignee);
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));

        // when
        taskExternalService.deleteTask(taskId, assignee.getId());

        // then
        assertThat(task.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteTask: 3자 삭제 시도 시 Forbidden")
    void deleteTask_shouldThrow_whenAnother() {
        // given
        Long taskId = 100L;
        given(userService.getUser(another.getId())).willReturn(another);
        given(taskRepository.findByIdAndDeletedAtIsNull(taskId)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskExternalService.deleteTask(taskId, another.getId()))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessage(TaskErrorCode.TSK_UPDATE_FAILED_FORBIDDEN.getMessage());

        assertThat(task.getDeletedAt()).isNull();
    }
}
