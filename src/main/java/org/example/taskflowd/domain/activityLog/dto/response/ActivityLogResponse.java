package org.example.taskflowd.domain.activityLog.dto.response;

import org.example.taskflowd.domain.activityLog.entity.ActivityLog;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;

import java.time.LocalDateTime;

public record ActivityLogResponse(Long id,
                                  ActLogEnum type,
                                  Long userId,
                                  UserResponseDto user,
                                  Long taskId,
                                  LocalDateTime createdAt,
                                  LocalDateTime timestamp,
                                  String description) {

    public static ActivityLogResponse from(ActivityLog activityLog) {

        return new ActivityLogResponse(
                activityLog.getId(),
                activityLog.getType(),
                activityLog.getUser().getId(),
                UserMapper.toResponseDto(activityLog.getUser()),
                activityLog.getTaskId(),
                activityLog.getCreatedAt(),
                activityLog.getCreatedAt(),
                activityLog.getDescription()
        );
    }
}
