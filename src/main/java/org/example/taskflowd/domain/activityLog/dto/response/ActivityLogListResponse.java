package org.example.taskflowd.domain.activityLog.dto.response;

import java.util.List;

public record ActivityLogListResponse(List<ActivityLogResponse> content) {

    public static ActivityLogListResponse of(List<ActivityLogResponse> content) {
        return new ActivityLogListResponse(content);
    }
}
