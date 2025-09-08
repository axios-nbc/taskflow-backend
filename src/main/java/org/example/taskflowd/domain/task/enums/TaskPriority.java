package org.example.taskflowd.domain.task.enums;

import lombok.Getter;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.exception.TaskErrorCode;

import java.util.Arrays;

@Getter
public enum TaskPriority {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    private final String code;

    TaskPriority(String code) {
        this.code = code;
    }

    public static TaskPriority select(String code) {
        return Arrays.stream(TaskPriority.values())
                .filter(p -> p.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_PRIORITY));
    }
}
