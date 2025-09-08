package org.example.taskflowd.doamin.task.service;

import org.example.taskflowd.domain.task.dto.request.TaskCreateRequest;
import org.example.taskflowd.domain.task.dto.response.TaskCreateResponse;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.mapper.TaskMapper;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.task.service.TaskExternalService;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.example.taskflowd.domain.user.service.UserService;
import org.example.taskflowd.domain.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@ExtendWith(MockitoExtension.class)
public class TaskExternalServiceTest {
    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;
    @Mock TaskMapper taskMapper;
    @Mock UserServiceImpl userService;


    @InjectMocks TaskExternalService taskExternalService;

    @Captor ArgumentCaptor<Task> taskCaptor;

    private User writer;
    private User assignee;
    private Task task;

    /* ========== Helper Method ========== */
    Task buildTask(String title, String description, User writer, User assignee) {
        return Task.builder()
                .title("Task")
                .description("Description")
                .writer(writer)
                .assignee(assignee)
                .status(null)
                .priority(null)
                .dueDate(null)
                .build();
    }
    Task buildTask(String title, String description, User writer, User assignee, TaskStatus status, TaskPriority priority, LocalDateTime dueDate) {
        return Task.builder()
                .title("Task")
                .description("Description")
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

        task = buildTask("작업", "내용", writer, assignee, TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now());
        ReflectionTestUtils.setField(task, "id", 100L);
    }

    @Test
    @DisplayName("createTask: 정상적인 request dto 입력시 response dto 반환")
    void createTask_shouldReturnTaskCreateResponseDto_whenGiveValidTaskCreateRequest() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        TaskCreateRequest req = new TaskCreateRequest("제목","내용", now, TaskPriority.LOW, assignee.getId());

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

        UserResponseDto assigneeDto = Mockito.mock(UserResponseDto.class);
        given(taskMapper.toCreateResponse(any(Task.class))).willAnswer(inv -> {
            Task t = inv.getArgument(0);
            return TaskCreateResponse.toDto(
                    t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(),
                    t.getPriority().getCode(), t.getStatus().getCode(),
                    t.getAssignee().getId(), assigneeDto, t.getCreatedAt(), t.getUpdatedAt()
            );
        });

        // when
        TaskCreateResponse taskCreateResponse = taskExternalService.createTask(req, writer.getId());

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
}
