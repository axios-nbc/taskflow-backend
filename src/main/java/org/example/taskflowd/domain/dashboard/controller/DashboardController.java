package org.example.taskflowd.domain.dashboard.controller;

import org.example.taskflowd.common.dto.response.ApiPageResponse;
import org.example.taskflowd.common.dto.response.ApiResponse;
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
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/my-tasks")
	public ResponseEntity<ApiResponse> getMyTasksSummary(
		@AuthenticationPrincipal User principal) {
		Long userId = Long.parseLong(principal.getUserName());
		MyTasksSummaryResponse summary = dashboardService.getMyTasksSummary(userId);
		return ApiResponse.ok("내 작업 요약 조회 완료", summary);
	}

	@GetMapping("/team-progress")
	public ResponseEntity<ApiResponse<TeamProgressResponse>> getTeamProgress(
		@AuthenticationPrincipal User principal) {
		Long userId = Long.parseLong(principal.getUserName());
		TeamProgressResponse progress = dashboardService.getTeamProgress(userId);
		return ApiResponse.ok("팀 진행률 조회 완료", progress);
	}

	@GetMapping("/activities")
	public ResponseEntity<ApiPageResponse<ActivityResponse>> getActivities(
		@AuthenticationPrincipal User principal,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = "10") int size) {

		Long userId = Long.parseLong(principal.getUsername());
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		return ApiPageResponse.success(dashboardService.getActivities(userId, pageable));
	}
}
