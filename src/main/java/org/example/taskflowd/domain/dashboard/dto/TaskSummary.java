package org.example.taskflowd.domain.dashboard.dto;

import java.time.LocalDateTime;

public record TaskSummary (
	Long id,
	String title,
	String status,
	LocalDateTime dueDate
) {
	public static TaskSummary of(Long id, String title, String status, LocalDateTime dueDate) {
		return new TaskSummary(id, title, status, dueDate);
	}
}
