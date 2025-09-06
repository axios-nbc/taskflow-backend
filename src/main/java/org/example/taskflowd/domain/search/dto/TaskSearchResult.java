package org.example.taskflowd.domain.search.dto;

import org.example.taskflowd.domain.user.dto.response.UserResponseDto;

public record TaskSearchResult(
	Long id,
	String title,
	String description,
	String status,
	UserResponseDto assignee
) {
	public static TaskSearchResult of(
		Long id, String title, String description, String status, UserResponseDto assignee) {
		return new TaskSearchResult(id, title, description, status, assignee);
	}
}
