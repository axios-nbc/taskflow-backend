package org.example.taskflowd.domain.dashboard.dto;

import java.util.Map;

public record TeamProgressResponse(
	Map<String, Integer> teamProgress
) {
	public static TeamProgressResponse of(Map<String, Integer> teamProgress) {
		return new TeamProgressResponse(teamProgress);
	}
}
