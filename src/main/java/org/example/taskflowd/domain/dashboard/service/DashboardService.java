package org.example.taskflowd.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.domain.dashboard.dto.ActivityResponse;
import org.example.taskflowd.domain.dashboard.dto.DashboardStatsResponse;
import org.example.taskflowd.domain.dashboard.dto.MyTasksSummaryResponse;
import org.example.taskflowd.domain.dashboard.dto.TaskSummary;
import org.example.taskflowd.domain.dashboard.dto.TeamProgressResponse;
import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.repository.ActivityLogRepository;
import org.example.taskflowd.domain.team.entity.TeamMember;
import org.example.taskflowd.domain.team.repository.TeamMemberRepository;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

	private final TaskRepository taskRepository;
	private final ActivityLogRepository activityLogRepository;
	private final TeamMemberRepository teamMemberRepository;

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
		List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
		List<Long> teamIds = memberships.stream().map(m -> m.getTeam().getId()).toList();

		Map<String, Integer> teamProgress = new HashMap<>();

		if (teamIds.isEmpty()) {
			return TeamProgressResponse.of(Collections.emptyMap());
		}

		List<Object[]> rows = teamMemberRepository.getTeamProgressStats(teamIds);
		for (Object[] r : rows) {
			String teamName = (String) r[0];
			long total = ((Number) r[1]).longValue();
			long done = ((Number) r[2]).longValue();
			int rate = total > 0 ? (int) Math.round(done * 100.0 / total) : 0;
			teamProgress.put(teamName, rate);
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
		Page<ActivityLog> activities = activityLogRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

		return activities.map(activity -> ActivityResponse.of(
			activity.getId(),
			activity.getUser().getId(),
			UserMapper.toResponseDto(activity.getUser()),
			activity.getType().name(),
			"TASK",
			activity.getTask().getId(),
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