package org.example.taskflowd.domain.dashboard.dto;

import java.util.List;

public record MyTasksSummaryResponse (
	List<TaskSummary> todayTasks,
	List<TaskSummary> upcomingTasks,
	List<TaskSummary> overdueTasks
) {
	public static MyTasksSummaryResponse of(
		List<TaskSummary> todayTasks,
		List<TaskSummary> upcomingTasks,
		List<TaskSummary> overdueTasks) {
		return new MyTasksSummaryResponse(todayTasks, upcomingTasks, overdueTasks);
	}
}
