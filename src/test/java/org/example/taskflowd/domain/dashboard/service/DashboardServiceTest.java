package org.example.taskflowd.domain.dashboard.service;

import org.example.taskflowd.domain.dashboard.dto.ActivityResponse;
import org.example.taskflowd.domain.dashboard.dto.MyTasksSummaryResponse;
import org.example.taskflowd.domain.dashboard.dto.TeamProgressResponse;
import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.activityLog.repository.ActivityLogRepository;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.team.entity.Team;
import org.example.taskflowd.domain.team.entity.TeamMember;
import org.example.taskflowd.domain.team.repository.TeamMemberRepository;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

	@Mock private TaskRepository taskRepository;
	@Mock private ActivityLogRepository activityLogRepository;
	@Mock private TeamMemberRepository teamMemberRepository;
	@Mock private UserService userService;

	@InjectMocks
	private DashboardService dashboardService;

	private final Long userId = 1L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("오늘 예정된 작업 요약 조회 - 정상 동작")
	void 오늘_예정_미완료_작업_요약_조회() {
		// Given
		LocalDateTime now = LocalDateTime.now();
		User testUser = new User("테스트", "testuser", "test@example.com", "password");
		
		Task todayTask = Task.builder()
			.title("오늘 할 일")
			.description("오늘 해야 할 작업")
			.writer(testUser)
			.assignee(testUser)
			.status(TaskStatus.TODO)
			.dueDate(now)
			.build();

		Task upcomingTask = Task.builder()
			.title("다가올 할 일")
			.description("다가올 작업")
			.writer(testUser)
			.assignee(testUser)
			.status(TaskStatus.TODO)
			.dueDate(now.plusDays(2))
			.build();

		Task overdueTask = Task.builder()
			.title("기한 지난 일")
			.description("기한이 지난 작업")
			.writer(testUser)
			.assignee(testUser)
			.status(TaskStatus.TODO)
			.dueDate(now.minusDays(2))
			.build();

		when(taskRepository.findByAssigneeIdAndDueDateBetween(anyLong(), any(), any()))
			.thenReturn(List.of(todayTask));

		when(taskRepository.findByAssigneeIdAndDueDateAfter(anyLong(), any()))
			.thenReturn(List.of(upcomingTask));

		when(taskRepository.findByAssigneeIdAndDueDateBefore(anyLong(), any()))
			.thenReturn(List.of(overdueTask));

		// When
		MyTasksSummaryResponse response = dashboardService.getMyTasksSummary(userId)
		;

		// Then
		assertThat(response.todayTasks()).hasSize(1);
		assertThat(response.todayTasks().get(0).title()).isEqualTo("오늘 할 일");
		assertThat(response.upcomingTasks()).hasSize(1);
		assertThat(response.upcomingTasks().get(0).title()).isEqualTo("다가올 할 일");
		assertThat(response.overdueTasks()).hasSize(1);
		assertThat(response.overdueTasks().get(0).title()).isEqualTo("기한 지난 일");

		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateBetween(eq(userId), any(), any());
		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateAfter(eq(userId), any());
		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateBefore(eq(userId), any());
	}

	@Test
	@DisplayName("팀 진행률 계산 - 정상 동작")
	void 팀_진행률_계산_정상동작() {
		// Given
		TeamMember membership = mock(TeamMember.class);
		Team team = mock(Team.class);
		when(team.getId()).thenReturn(10L);
		when(team.getName()).thenReturn("개발팀");
		when(membership.getTeam()).thenReturn(team);
		when(teamMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));

		List<Object[]> stats = new java.util.ArrayList<>();
		stats.add(new Object[]{ "개발팀", 10L, 5L });
		when(teamMemberRepository.getTeamProgressStats(anyList())).thenReturn(stats);

		// When
		TeamProgressResponse response = dashboardService.getTeamProgress(userId);

		// Then
		assertThat(response.teamProgress()).containsEntry("개발팀", 50);
		assertThat(response.teamProgress()).hasSize(1);

		verify(teamMemberRepository, times(1)).findByUserId(userId);
		verify(teamMemberRepository, times(1)).getTeamProgressStats(List.of(10L));
	}

	@Test
	@DisplayName("팀이 없을 때 빈 맵 반환")
	void 팀이_없을때_빈맵_반환() {
		// Given
		when(teamMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

		// When
		TeamProgressResponse response = dashboardService.getTeamProgress(userId);

		// Then
		assertThat(response.teamProgress()).isEmpty();

		verify(teamMemberRepository, times(1)).findByUserId(userId);
		verify(teamMemberRepository, never()).getTeamProgressStats(anyList());
	}

	@Test
	@DisplayName("활동 내역 조회 - Page 매핑 정상 동작")
	void 활동내역_조회시_Page_매핑_정상동작() {
		// Given
		User user = new User("테스트", "testuser", "test@example.com", "password");

		Task task = Task.builder()
			.title("t").description("d").writer(user).assignee(user).status(TaskStatus.TODO)
			.dueDate(LocalDateTime.now()).build();
		ActivityLog activity = ActivityLog.create(ActLogEnum.TASK_CREATED, user, task, "새 작업 생성");

		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		Page<ActivityLog> page = new PageImpl<>(List.of(activity), pageable, 1);

		when(activityLogRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable))
			.thenReturn(page);

		// When
		Page<ActivityResponse> response = dashboardService.getActivities(userId, pageable);

		// Then
		assertThat(response).hasSize(1);
		assertThat(response.getContent().get(0).action()).isEqualTo("TASK_CREATED");
		assertThat(response.getContent().get(0).targetType()).isEqualTo("TASK");
		assertThat(response.getContent().get(0).description()).isEqualTo("새 작업 생성");

		verify(activityLogRepository, times(1)).findByUser_IdOrderByCreatedAtDesc(userId, pageable);
	}

	@Test
	@DisplayName("작업 요약 조회 - 완료된 작업은 제외")
	void 작업_요약_조회_완료된_작업_제외() {
		// Given
		LocalDateTime now = LocalDateTime.now();
		User testUser = new User("테스트", "testuser", "test@example.com", "password");
		
		Task completedTask = Task.builder()
			.title("완료된 작업")
			.description("완료된 작업 설명")
			.writer(testUser)
			.assignee(testUser)
			.status(TaskStatus.COMPLETE)
			.dueDate(now.plusDays(1))
			.build();

		Task pendingTask = Task.builder()
			.title("진행 중인 작업")
			.description("진행 중인 작업 설명")
			.writer(testUser)
			.assignee(testUser)
			.status(TaskStatus.IN_PROGRESS)
			.dueDate(now.plusDays(1))
			.build();

		when(taskRepository.findByAssigneeIdAndDueDateBetween(anyLong(), any(), any()))
			.thenReturn(Collections.emptyList());

		when(taskRepository.findByAssigneeIdAndDueDateAfter(anyLong(), any()))
			.thenReturn(List.of(completedTask, pendingTask));

		when(taskRepository.findByAssigneeIdAndDueDateBefore(anyLong(), any()))
			.thenReturn(Collections.emptyList());

		// When
		MyTasksSummaryResponse response = dashboardService.getMyTasksSummary(userId);

		// Then
		assertThat(response.upcomingTasks()).hasSize(1);
		assertThat(response.upcomingTasks().get(0).title()).isEqualTo("진행 중인 작업");
		assertThat(response.upcomingTasks().get(0).status()).isEqualTo(TaskStatus.IN_PROGRESS.getCode());
	}

	@Test
	@DisplayName("팀 진행률 계산 - 여러 팀")
	void 팀_진행률_계산_여러팀() {
		// Given
		TeamMember m1 = mock(TeamMember.class);
		TeamMember m2 = mock(TeamMember.class);
		Team t1 = mock(Team.class);
		Team t2 = mock(Team.class);
		when(t1.getId()).thenReturn(10L);
		when(t1.getName()).thenReturn("개발팀");
		when(t2.getId()).thenReturn(20L);
		when(t2.getName()).thenReturn("디자인팀");
		when(m1.getTeam()).thenReturn(t1);
		when(m2.getTeam()).thenReturn(t2);
		when(teamMemberRepository.findByUserId(userId)).thenReturn(List.of(m1, m2));

		List<Object[]> stats = new java.util.ArrayList<>();
		stats.add(new Object[]{ "개발팀", 10L, 8L });
		stats.add(new Object[]{ "디자인팀", 5L, 3L });
		when(teamMemberRepository.getTeamProgressStats(anyList())).thenReturn(stats);

		// When
		TeamProgressResponse response = dashboardService.getTeamProgress(userId);

		// Then
		assertThat(response.teamProgress()).containsEntry("개발팀", 80);
		assertThat(response.teamProgress()).containsEntry("디자인팀", 60);
		assertThat(response.teamProgress()).hasSize(2);
	}

	@Test
	@DisplayName("활동 내역 조회 - 빈 결과")
	void 활동내역_조회_빈결과() {
		// Given
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		Page<ActivityLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

		when(activityLogRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable))
			.thenReturn(emptyPage);

		// When
		Page<ActivityResponse> response = dashboardService.getActivities(userId, pageable);

		// Then
		assertThat(response).isEmpty();
		assertThat(response.getTotalElements()).isEqualTo(0);

		verify(activityLogRepository, times(1)).findByUser_IdOrderByCreatedAtDesc(userId, pageable);
	}

	@Test
	@DisplayName("작업 요약 조회 - 모든 카테고리 빈 결과")
	void 작업_요약_조회_모든_카테고리_빈결과() {
		// Given
		when(taskRepository.findByAssigneeIdAndDueDateBetween(anyLong(), any(), any()))
			.thenReturn(Collections.emptyList());

		when(taskRepository.findByAssigneeIdAndDueDateAfter(anyLong(), any()))
			.thenReturn(Collections.emptyList());

		when(taskRepository.findByAssigneeIdAndDueDateBefore(anyLong(), any()))
			.thenReturn(Collections.emptyList());

		// When
		MyTasksSummaryResponse response = dashboardService.getMyTasksSummary(userId);

		// Then
		assertThat(response.todayTasks()).isEmpty();
		assertThat(response.upcomingTasks()).isEmpty();
		assertThat(response.overdueTasks()).isEmpty();

		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateBetween(eq(userId), any(), any());
		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateAfter(eq(userId), any());
		verify(taskRepository, times(1)).findByAssigneeIdAndDueDateBefore(eq(userId), any());
	}
}
