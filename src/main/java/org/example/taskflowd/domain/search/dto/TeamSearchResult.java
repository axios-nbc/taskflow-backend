package org.example.taskflowd.domain.search.dto;

public record TeamSearchResult(
	Long id,
	String name,
	String description
) {
	public static TeamSearchResult of(Long id, String name, String description) {
		return new TeamSearchResult(id, name, description);
	}
}
