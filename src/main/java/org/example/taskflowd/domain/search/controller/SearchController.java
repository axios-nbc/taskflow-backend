package org.example.taskflowd.domain.search.controller;

import org.example.taskflowd.common.dto.response.ApiPageResponse;
import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.domain.search.dto.IntegratedSearchResponse;
import org.example.taskflowd.domain.task.dto.response.TaskListItemResponse;
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

		Long userId = Long.parseLong(principal.getUsername());
		IntegratedSearchResponse result = searchService.integratedSearch(query, userId);
		return ApiResponse.ok(ResponseMessage.INTEGRATED_SEARCH_COMPLETED, result);
	}

	@GetMapping("/tasks/search")
	public ResponseEntity<ApiPageResponse<TaskListItemResponse>> searchTasks(
		@RequestParam("q") String query,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = "10") int size,
		@AuthenticationPrincipal User principal) {

		Long userId = Long.parseLong(principal.getUsername());
		Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

		return ApiPageResponse.success(searchService.searchTasks(query, userId, pageable));
	}
}
