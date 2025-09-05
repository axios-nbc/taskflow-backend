package org.example.taskflowd.domain.dashboard.dto;

import java.time.LocalDateTime;

import org.example.taskflowd.domain.user.dto.response.UserResponseDto;

public record ActivityResponse(
	Long id,
	Long userId,
	UserResponseDto user,
	String action,
	String targetType,
	Long targetId,
	String description,
	LocalDateTime createdAt
) {
	public static ActivityResponse of(
		Long id,
		Long userId,
		UserResponseDto user,
		String action,
		String targetType,
		Long targetId,
		String description,
		LocalDateTime createdAt) {
		return new ActivityResponse(id, userId, user, action, targetType, targetId, description, createdAt);
	}
}
