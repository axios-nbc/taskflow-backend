package org.example.taskflowd.domain.search.service;

import org.example.taskflowd.domain.search.dto.IntegratedSearchResponse;
import org.example.taskflowd.domain.search.dto.TeamSearchResult;
import org.example.taskflowd.domain.task.dto.response.TaskListItemResponse;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.team.entity.Team;
import org.example.taskflowd.domain.team.repository.TeamRepository;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;

    @InjectMocks
    private SearchService searchService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("통합 검색 - tasks/users/teams 상위 10건 반환")
    void 통합검색_정상() {
        // Given
        User u = new User("u", "user1", "u@e.com", "p");
        Team t = new Team("개발팀", "백엔드");

        Task task = Task.builder()
                .title("Fix login")
                .description("bug")
                .writer(u)
                .assignee(u)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDateTime.now())
                .build();

        Page<Task> taskPage = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
        when(taskRepository.findTopByQueryAndAssigneeId(anyString(), eq(userId), any(Pageable.class)))
                .thenReturn(taskPage);

        when(userRepository.searchUsers(eq("dev"), any(Pageable.class)))
                .thenReturn(List.of(u));

        when(teamRepository.searchTeams(eq("dev"), any(Pageable.class)))
                .thenReturn(List.of(t));

        // When
        IntegratedSearchResponse res = searchService.integratedSearch("dev", userId);

        // Then
        assertThat(res.tasks()).hasSize(1);
        assertThat(res.users()).extracting("username").containsExactly("user1");
        assertThat(res.teams()).extracting(TeamSearchResult::name).containsExactly("개발팀");
    }

    @Test
    @DisplayName("작업 검색 - 쿼리 없으면 내 작업 페이지 반환")
    void 작업검색_쿼리없음() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(), pageable, 0);
        when(taskRepository.findByAssigneeId(eq(userId), eq(pageable))).thenReturn(taskPage);

        // When
        Page<TaskListItemResponse> res = searchService.searchTasks("", userId, pageable);

        // Then
        assertThat(res.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("작업 검색 - 상태/우선순위 필터")
    void 작업검색_필터() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(), pageable, 0);
        when(taskRepository.findByAssigneeIdAndStatus(eq(userId), eq(TaskStatus.TODO), eq(pageable)))
                .thenReturn(taskPage);

        // When
        Page<TaskListItemResponse> res = searchService.searchTasksWithFilters(null, userId, "todo", null, pageable);

        // Then
        assertThat(res.getTotalElements()).isZero();
    }
}


