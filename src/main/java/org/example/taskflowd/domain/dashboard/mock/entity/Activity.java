package org.example.taskflowd.domain.dashboard.mock.entity;

import java.time.LocalDateTime;

import org.example.taskflowd.domain.user.entity.User;

public class Activity {
	private Long id;
	private User user;
	private String action;
	private String targetType;
	private Long targetId;
	private String description;
	private LocalDateTime createdAt;

	// Getters
	public Long getId() { return id; }
	public User getUser() { return user; }
	public String getAction() { return action; }
	public String getTargetType() { return targetType; }
	public Long getTargetId() { return targetId; }
	public String getDescription() { return description; }
	public LocalDateTime getCreatedAt() { return createdAt; }

	// Setters for mock data
	public void setId(Long id) { this.id = id; }
	public void setUser(User user) { this.user = user; }
	public void setAction(String action) { this.action = action; }
	public void setTargetType(String targetType) { this.targetType = targetType; }
	public void setTargetId(Long targetId) { this.targetId = targetId; }
	public void setDescription(String description) { this.description = description; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
