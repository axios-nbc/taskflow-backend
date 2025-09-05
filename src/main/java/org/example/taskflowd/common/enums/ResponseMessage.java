package org.example.taskflowd.common.enums;

import lombok.Getter;

public interface ResponseMessage {
    String getMessage();
    
    ResponseMessage MY_TASKS_SUMMARY_INQUIRE = () -> "내 작업 요약 조회가 완료되었습니다.";
    ResponseMessage TEAM_PROGRESS_INQUIRE = () -> "팀 진행상황 조회가 완료되었습니다.";
    ResponseMessage INTEGRATED_SEARCH_COMPLETED = () -> "통합 검색이 완료되었습니다.";
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
