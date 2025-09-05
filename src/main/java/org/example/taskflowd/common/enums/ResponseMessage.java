package org.example.taskflowd.common.enums;

import lombok.Getter;

public interface ResponseMessage {
    String getMessage();
}

/** Message 예시 */
//public enum DomainResponseMessage implements ResponseMessage {
//    DOMAIN_STATUS("message"),
//    // example
//    COMMENT_UPDATED("댓글이 수정되었습니다.");
//
//    private final String message;
//
//    DomainResponseMessage(String message) {
//        this.message = message;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//}
