package org.example.taskflowd.domain.dashboard.dto;

import java.time.LocalDate;

public record WeeklyTrendItem(
	String name,
	int tasks,
	int completed,
	LocalDate date
) {
	public static WeeklyTrendItem of(String name, int tasks, int completed, LocalDate date) {
		return new WeeklyTrendItem(name, tasks, completed, date);
	}
}
