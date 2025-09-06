package org.example.taskflowd.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.domain.dashboard.dto.ActivityResponse;
import org.example.taskflowd.domain.dashboard.dto.DashboardStatsResponse;
import org.example.taskflowd.domain.dashboard.dto.MyTasksSummaryResponse;
import org.example.taskflowd.domain.dashboard.dto.TaskSummary;
import org.example.taskflowd.domain.dashboard.dto.TeamProgressResponse;
import org.example.taskflowd.domain.dashboard.mock.entity.Activity;
import org.example.taskflowd.domain.dashboard.mock.entity.Team;
import org.example.taskflowd.domain.dashboard.mock.repository.ActivityRepositoryMock;
import org.example.taskflowd.domain.dashboard.mock.repository.TeamMemberRepositoryMock;
import org.example.taskflowd.domain.dashboard.mock.repository.TeamRepositoryMock;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	private final TaskRepository taskRepository;
	private final ActivityRepositoryMock activityRepository;
	private final TeamRepositoryMock teamRepository;
	private final TeamMemberRepositoryMock teamMemberRepository;

	public MyTasksSummaryResponse getMyTasksSummary(Long userId) {
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime startOfDay = today.with(LocalTime.MIN);
		LocalDateTime endOfDay = today.with(LocalTime.MAX);
		LocalDateTime tomorrow = today.plusDays(1);

		List<TaskSummary> todayTasks = taskRepository.findByAssigneeIdAndDueDateBetween(
				userId, startOfDay, endOfDay)
			.stream()
			.map(this::toTaskSummary)
			.collect(Collectors.toList());

		List<TaskSummary> upcomingTasks = taskRepository.findByAssigneeIdAndDueDateAfter(userId, tomorrow)
			.stream()
			.filter(task -> !task.getStatus().equals(TaskStatus.COMPLETE))
			.limit(5)
			.map(this::toTaskSummary)
			.collect(Collectors.toList());

		List<TaskSummary> overdueTasks = taskRepository.findByAssigneeIdAndDueDateBefore(userId, startOfDay)
			.stream()
			.filter(task -> !task.getStatus().equals(TaskStatus.COMPLETE))
			.map(this::toTaskSummary)
			.collect(Collectors.toList());

		return MyTasksSummaryResponse.of(todayTasks, upcomingTasks, overdueTasks);
	}

	public TeamProgressResponse getTeamProgress(Long userId) {
		List<Team> userTeams = teamRepository.findTeamsByUserId(userId);
		List<Long> teamIds = userTeams.stream().map(Team::getId).collect(Collectors.toList());

		Map<String, Integer> teamProgress = new HashMap<>();

		if (!teamIds.isEmpty()) {
			List<Object[]> progressStats = teamMemberRepository.getTeamProgressStats(teamIds);

			for (Object[] stat : progressStats) {
				String teamName = (String) stat[0];
				Long totalTasks = (Long) stat[1];
				Long completedTasks = (Long) stat[2];

				int progressPercentage = totalTasks > 0 ?
					(int) ((completedTasks * 100) / totalTasks) : 0;

				teamProgress.put(teamName, progressPercentage);
			}
		} else {
			// Mock 데이터 - 팀 기능이 없을 때 샘플 데이터 제공
			teamProgress.put("개발팀", 75);
			teamProgress.put("디자인팀", 60);
			teamProgress.put("QA팀", 85);
		}

		return TeamProgressResponse.of(teamProgress);
	}

	public DashboardStatsResponse getStats(Long userId) {
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime startOfDay = today.with(LocalTime.MIN);
		LocalDateTime endOfDay = today.with(LocalTime.MAX);

		TaskStatus done = TaskStatus.COMPLETE;
		TaskStatus inProgress = TaskStatus.IN_PROGRESS;
		TaskStatus todo = TaskStatus.TODO;

		long total = taskRepository.countByAssigneeIdAndDeletedAtIsNull(userId);
		long completed = taskRepository.countByAssigneeIdAndStatusAndDeletedAtIsNull(userId, done);
		long inProg = taskRepository.countByAssigneeIdAndStatusAndDeletedAtIsNull(userId, inProgress);
		long todoCnt = taskRepository.countByAssigneeIdAndStatusAndDeletedAtIsNull(userId, todo);
		long overdue = taskRepository.countByAssigneeIdAndDueDateBeforeAndStatusNotAndDeletedAtIsNull(userId, startOfDay, done);
		long myToday = taskRepository.countByAssigneeIdAndDueDateBetweenAndDeletedAtIsNull(userId, startOfDay, endOfDay);

		TeamProgressResponse teamProgressResponse = getTeamProgress(userId);
		int teamProgress = 0;
		if (teamProgressResponse != null && teamProgressResponse.teamProgress() != null && !teamProgressResponse.teamProgress().isEmpty()) {
			teamProgress = (int) Math.round(
				teamProgressResponse.teamProgress().values().stream()
					.mapToInt(Integer::intValue)
					.average()
					.orElse(0.0)
			);
		}

		int completionRate = total > 0 ? (int) Math.round((completed * 100.0) / total) : 0;

		return new DashboardStatsResponse(
			total, completed, inProg, todoCnt, overdue, teamProgress, myToday, completionRate
		);
	}

	public Page<ActivityResponse> getActivities(Long userId, Pageable pageable) {
		Page<Activity> activities = activityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

		return activities.map(activity -> ActivityResponse.of(
			activity.getId(),
			activity.getUser() != null ? activity.getUser().getId() : userId,
			activity.getUser() != null ? UserMapper.toResponseDto(activity.getUser()) : null,
			activity.getAction(),
			activity.getTargetType(),
			activity.getTargetId(),
			activity.getDescription(),
			activity.getCreatedAt()
		));
	}

	private TaskSummary toTaskSummary(Task task) {
		return TaskSummary.of(
			task.getId(),
			task.getTitle(),
			task.getStatus().getCode(),
			task.getDueDate()
		);
	}
}