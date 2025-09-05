package org.example.taskflowd.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.domain.search.dto.IntegratedSearchResponse;
import org.example.taskflowd.domain.search.dto.TaskSearchResult;
import org.example.taskflowd.domain.search.dto.TeamSearchResult;
import org.example.taskflowd.domain.search.dto.UserSearchResult;
import org.example.taskflowd.domain.task.dto.response.TaskListItemResponse;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

	private final TaskRepository taskRepository;

	public IntegratedSearchResponse integratedSearch(String query, Long userId) {
		List<TaskSearchResult> tasks = searchTasksInternal(query, userId)
			.stream()
			.map(this::toTaskSearchResult)
			.collect(Collectors.toList());

		List<UserSearchResult> users = Collections.emptyList();
		List<TeamSearchResult> teams = Collections.emptyList();

		return IntegratedSearchResponse.of(tasks, users, teams);
	}

	public Page<TaskListItemResponse> searchTasks(String query, Long userId, Pageable pageable) {
		Page<Task> tasks;
		
		if (StringUtils.hasText(query)) {
			tasks = taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAssigneeId(
				query, userId, pageable);
		} else {
			tasks = taskRepository.findByAssigneeId(userId, pageable);
		}
		
		return tasks.map(this::toTaskListItemResponse);
	}

	public Page<TaskListItemResponse> searchTasksWithFilters(String query, Long userId, 
			String status, String priority, Pageable pageable) {
		
		Page<Task> tasks;
		
		if (!StringUtils.hasText(query)) {
			tasks = getTasksByFilters(userId, status, priority, pageable);
		} else {
			tasks = getTasksByQueryAndFilters(query, userId, status, priority, pageable);
		}
		
		return tasks.map(this::toTaskListItemResponse);
	}

	private Page<Task> getTasksByFilters(Long userId, String status, String priority, Pageable pageable) {
		TaskStatus taskStatus = parseTaskStatus(status);
		TaskPriority taskPriority = parseTaskPriority(priority);
		
		if (taskStatus != null && taskPriority != null) {
			return taskRepository.findByAssigneeIdAndStatusAndPriority(userId, taskStatus, taskPriority, pageable);
		} else if (taskStatus != null) {
			return taskRepository.findByAssigneeIdAndStatus(userId, taskStatus, pageable);
		} else if (taskPriority != null) {
			return taskRepository.findByAssigneeIdAndPriority(userId, taskPriority, pageable);
		} else {
			return taskRepository.findByAssigneeId(userId, pageable);
		}
	}

	private Page<Task> getTasksByQueryAndFilters(String query, Long userId, String status, String priority, Pageable pageable) {
		TaskStatus taskStatus = parseTaskStatus(status);
		TaskPriority taskPriority = parseTaskPriority(priority);
		
		if (taskStatus != null && taskPriority != null) {
			return taskRepository.findByQueryAndAssigneeIdAndStatusAndPriority(query, userId, taskStatus, taskPriority, pageable);
		} else if (taskStatus != null) {
			return taskRepository.findByQueryAndAssigneeIdAndStatus(query, userId, taskStatus, pageable);
		} else if (taskPriority != null) {
			return taskRepository.findByQueryAndAssigneeIdAndPriority(query, userId, taskPriority, pageable);
		} else {
			return taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAssigneeId(query, userId, pageable);
		}
	}

	private List<Task> searchTasksInternal(String query, Long userId) {
		if (!StringUtils.hasText(query)) {
			return Collections.emptyList();
		}
		
		Page<Task> tasks = taskRepository.findTopByQueryAndAssigneeId(query, userId, 
			PageRequest.of(0, 10));
		return tasks.getContent();
	}

	private TaskSearchResult toTaskSearchResult(Task task) {
		return TaskSearchResult.of(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getStatus().getCode(),
			UserMapper.toResponseDto(task.getAssignee())
		);
	}

	private TaskListItemResponse toTaskListItemResponse(Task task) {
		return TaskListItemResponse.toDto(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getDueDate(),
			task.getPriority().getCode(),
			task.getStatus().getCode(),
			task.getAssignee().getId(),
			UserMapper.toResponseDto(task.getAssignee()),
			task.getCreatedAt(),
			task.getUpdatedAt()
		);
	}

	private TaskStatus parseTaskStatus(String status) {
		if (!StringUtils.hasText(status)) {
			return null;
		}
		try {
			return TaskStatus.select(status);
		} catch (Exception e) {
			return null;
		}
	}

	private TaskPriority parseTaskPriority(String priority) {
		if (!StringUtils.hasText(priority)) {
			return null;
		}
		try {
			return TaskPriority.select(priority);
		} catch (Exception e) {
			return null;
		}
	}
}
