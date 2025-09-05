package org.example.taskflowd.domain.search.dto;
import java.util.List;

public record IntegratedSearchResponse(
	List<TaskSearchResult> tasks,
	List<UserSearchResult> users,
	List<TeamSearchResult> teams
) {
	public static IntegratedSearchResponse of(
		List<TaskSearchResult> tasks,
		List<UserSearchResult> users,
		List<TeamSearchResult> teams) {
		return new IntegratedSearchResponse(tasks, users, teams);
	}
}
