package org.example.taskflowd.domain.task.dto.response;

import java.util.List;

public record TaskPageResponse(List<TaskListItemResponse> content) {

    public static TaskPageResponse of(List<TaskListItemResponse> content) {
        return new TaskPageResponse(content);
    }
}
