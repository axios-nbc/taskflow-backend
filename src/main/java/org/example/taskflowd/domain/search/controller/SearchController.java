package org.example.taskflowd.domain.search.controller;

import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.common.dto.response.PageData;
import org.example.taskflowd.common.enums.ResponseMessage;
import org.example.taskflowd.domain.search.dto.IntegratedSearchResponse;
import org.example.taskflowd.domain.search.service.SearchService;
import org.example.taskflowd.domain.task.dto.response.TaskListItemResponse;
import org.example.taskflowd.domain.task.enums.TaskResponseMessage;
import org.example.taskflowd.domain.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<IntegratedSearchResponse>> integratedSearch(
		@RequestParam("q") String query,
		@AuthenticationPrincipal User principal) {

		Long userId = Long.parseLong(principal.getUserName());
		IntegratedSearchResponse result = searchService.integratedSearch(query, userId);
		return ApiResponse.ok(ResponseMessage.INTEGRATED_SEARCH_COMPLETED, result);
	}

	@GetMapping("/tasks/search")
	public ResponseEntity<ApiResponse<PageData<TaskListItemResponse>>> searchTasks(
		@RequestParam("q") String query,
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String priority,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = "10") int size,
		@RequestParam(required = false, defaultValue = "updatedAt") String sortBy,
		@RequestParam(required = false, defaultValue = "desc") String sortDir,
		@AuthenticationPrincipal User principal) {

		Long userId = Long.parseLong(principal.getUserName());
		Pageable pageable = createPageable(page, size, sortBy, sortDir);


        return ApiResponse.ok(
                TaskResponseMessage.TASK_LIST_INQUIRE,
                (status != null || priority != null) ?
				    PageData.from(searchService.searchTasksWithFilters(query, userId, status, priority, pageable)) :
                        PageData.from(searchService.searchTasks(query, userId, pageable))
        );
	}

	private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
			Sort.Direction.DESC : Sort.Direction.ASC;
		String safe = switch (sortBy) {
			case "updatedAt", "dueDate", "createdAt", "priority", "title" -> sortBy;
			default -> "updatedAt";
		};
		Sort sort = Sort.by(direction, safe);
		return PageRequest.of(page, size, sort);
	}
}
