package org.example.taskflowd.domain.dashboard.dto;

public record DashboardStatsResponse(
	long totalTasks,
	long completedTasks,
	long inProgressTasks,
	long todoTasks,
	long overdueTasks,
	int teamProgress,
	long myTasksToday,
	int completionRate
) {}
