package org.example.taskflowd.common.enums;

import lombok.Getter;

// 각자 Message를 명세서에 따라 등록하시면 됩니다!
@Getter
public enum ResponseMessage {
    // TASK
    TASK_CREATED("Task가 생성되었습니다."),
    TASK_LIST_INQUIRE("Task 목록을 조회했습니다."),
    TASK_OBJECT_INQUIRE("Task를 조회했습니다."),
    TASK_CONTENT_UPDATED("Task가 수정되었습니다."),
    TASK_STATUS_UPDATED("작업 상태가 업데이트되었습니다."),
    TASK_DELETED("Task가 삭제되었습니다."),

    COMMENT_CREATED("댓글이 생성되었습니다."),
    COMMENT_LIST_INQUIRE("댓글 목록을 조회했습니다."),
    COMMENT_UPDATED("댓글이 수정되었습니다."),
    COMMENT_DELETE("댓글이 삭제되었습니다."),
    COMMENT_DELETE_WITH_CHILD("댓글과 대댓글들이 삭제되었습니다."),

    MY_TASKS_SUMMARY_INQUIRE("내 작업 요약을 조회했습니다."),
    TEAM_PROGRESS_INQUIRE("팀 진행률을 조회했습니다."),
    INTEGRATED_SEARCH_COMPLETED("통합 검색이 완료되었습니다.");

    private final String message;

    ResponseMessage(String message) {
        this.message = message;
    }

}
