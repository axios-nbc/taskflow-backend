package org.example.taskflowd.domain.task.enums;

import org.example.taskflowd.common.enums.ResponseMessage;

public enum TaskResponseMessage implements ResponseMessage {
    // TASK
    TASK_CREATED("Task가 생성되었습니다."),
    TASK_LIST_INQUIRE("Task 목록을 조회했습니다."),
    TASK_OBJECT_INQUIRE("Task를 조회했습니다."),
    TASK_CONTENT_UPDATED("Task가 수정되었습니다."),
    TASK_STATUS_UPDATED("작업 상태가 업데이트되었습니다."),
    TASK_DELETED("Task가 삭제되었습니다.");

    private final String message;

    TaskResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
