package org.example.taskflowd.domain.dashboard.mock.entity;

public class Team {
	private Long id;
	private String name;
	private String description;

	// Getters
	public Long getId() { return id; }
	public String getName() { return name; }
	public String getDescription() { return description; }

	// Setters for mock data
	public void setId(Long id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
}
