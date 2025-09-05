package org.example.taskflowd.domain.comment.enums;

import org.example.taskflowd.common.enums.ResponseMessage;

public enum CommentResponseMessage implements ResponseMessage {
    // Comment
    COMMENT_CREATED("댓글이 생성되었습니다."),
    COMMENT_LIST_INQUIRE("댓글 목록을 조회했습니다."),
    COMMENT_UPDATED("댓글이 수정되었습니다."),
    COMMENT_DELETE("댓글이 삭제되었습니다."),
    COMMENT_DELETE_WITH_CHILD("댓글과 대댓글들이 삭제되었습니다.");

    private final String message;

    CommentResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
