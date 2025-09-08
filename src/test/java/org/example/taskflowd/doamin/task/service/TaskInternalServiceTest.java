package org.example.taskflowd.doamin.task.service;

import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.exception.TaskErrorCode;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.task.service.TaskInternalServiceImpl;
import org.example.taskflowd.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


@ExtendWith(MockitoExtension.class)
public class TaskInternalServiceTest {
    @Mock TaskRepository taskRepository;

    @InjectMocks TaskInternalServiceImpl taskInternalService;

    private User writer;
    private User assignee;
    private Task task;

    @BeforeEach
    void setUp() {
        writer = new User(
                "writer", "writer123", "a@example.com", "123456");
        ReflectionTestUtils.setField(writer, "id", 10L);
        assignee = new User("assignee", "assignee123", "b@example.com", "55AA55");
        ReflectionTestUtils.setField(assignee, "id", 20L);

        task = Task.builder()
                .title("Task")
                .description("Description")
                .writer(writer)
                .assignee(assignee)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(task, "id", 100L);
    }

    @Test
    @DisplayName("getTaskByIdOrThrow: 존재하는 ID일 때 해당 Task 반환")
    void getTaskByIdOrThrow_shouldReturnTask_whenGiveValidId() {
        // given
        Long id = 100L;
        given(taskRepository.findByIdAndDeletedAtIsNull(id)).willReturn(Optional.of(task));

        // when
        Task res = taskInternalService.getTaskByIdOrThrow(id);

        // then
        assertThat(res.getId()).isEqualTo(id);
        assertThat(res.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    @DisplayName("getTaskByIdOrThrow: 존재하지 않는 Id일때 TSK_SEARCH_FAILED_INVALID_ID")
    void getTaskByIdOrThrow_shouldThrowError_whenGiveInvalidId() {
        // given
        Long invalidId = 1L;
        given(taskRepository.findByIdAndDeletedAtIsNull(invalidId)).willReturn(Optional.empty());

        // when
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(
                () -> taskInternalService.getTaskByIdOrThrow(invalidId)
        );

        // then
        assertThat(thrown)
                .isInstanceOf(InvalidTaskException.class)
                .extracting("errorCode")
                .isEqualTo(TaskErrorCode.TSK_SEARCH_FAILED_INVALID_ID);

        assertThat(thrown)
                .extracting("errorCode.code", "errorCode.httpStatus", "errorCode.message")
                .containsExactly(
                        "TSK-001",
                        HttpStatus.NOT_FOUND,
                        "해당 ID의 작업을 찾을 수 없습니다."
                );
    }
}
