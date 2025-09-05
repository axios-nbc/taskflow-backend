package org.example.taskflowd.domain.search.dto;

public record UserSearchResult(
	Long id,
	String username,
	String name,
	String email
) {
	public static UserSearchResult of(Long id, String username, String name, String email) {
		return new UserSearchResult(id, username, name, email);
	}
}
