package org.example.taskflowd.domain.activityLog.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.common.security.AuthUser;
import org.example.taskflowd.domain.activityLog.dto.response.ActivityLogListResponse;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.activityLog.service.ActivityLogInternalService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ActivityLogController {

    private final ActivityLogInternalService activityLogInternalService;

    @GetMapping("/activities")
    public ResponseEntity<?> getActivityLogs(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,    // { size, page }의 기본값 -> { 10, 0 }
            @RequestParam(required = false) ActLogEnum type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

        ActivityLogListResponse response = activityLogInternalService.getActivityLogs(pageable, type, userId, taskId, startDate, endDate);
        return ApiResponse.ok(response);
    }

    @GetMapping("/activities/my")
    public ResponseEntity<?> getMyActivityLogs(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                               @AuthenticationPrincipal AuthUser authUser) {

        ActivityLogListResponse response = activityLogInternalService.getActivityLogs(pageable, null, authUser.id(), null, null, null);
        return ApiResponse.ok(response);
    }
}