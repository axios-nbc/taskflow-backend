package org.example.taskflowd.domain.task.enums;

import lombok.Getter;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.exception.TaskErrorCode;

import java.util.Arrays;

@Getter
public enum TaskStatus {
    TODO("TODO"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE");

    private final String code;

    TaskStatus(String code) {
        this.code = code;
    }

    public static TaskStatus select(String code) {
        return Arrays.stream(TaskStatus.values())
                .filter(s -> s.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new InvalidTaskException(TaskErrorCode.TSK_UPDATE_FAILED_INVALID_STATUS));
    }
}
