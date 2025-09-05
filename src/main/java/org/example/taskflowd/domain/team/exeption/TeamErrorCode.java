package org.example.taskflowd.domain.team.exeption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements ErrorCode {
    // Team 관련 에러
    TEAM_NOT_FOUND("TEAM_001", HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."),
    TEAM_NAME_DUPLICATE("TEAM_002", HttpStatus.CONFLICT, "이미 존재하는 팀 이름입니다."),
    TEAM_NAME_EMPTY("TEAM_003", HttpStatus.BAD_REQUEST, "팀 이름은 필수입니다."),
    TEAM_ALREADY_DELETED("TEAM_004", HttpStatus.BAD_REQUEST, "이미 삭제된 팀입니다."),

    // TeamMember 관련 에러
    MEMBER_NOT_FOUND("MEMBER_001", HttpStatus.NOT_FOUND, "존재하지 않는 팀 멤버입니다."),
    MEMBER_ALREADY_EXISTS("MEMBER_002", HttpStatus.CONFLICT, "이미 팀에 참여한 사용자입니다."),
    MEMBER_NOT_IN_TEAM("MEMBER_003", HttpStatus.BAD_REQUEST, "해당 팀의 멤버가 아닙니다."),
    MEMBER_CANNOT_LEAVE_ADMIN("MEMBER_004", HttpStatus.BAD_REQUEST, "관리자는 팀을 떠날 수 없습니다."),

    // 권한 관련 에러
    INSUFFICIENT_PERMISSION("AUTH_001", HttpStatus.FORBIDDEN, "권한이 부족합니다."),
    CANNOT_CHANGE_OWN_ROLE("AUTH_002", HttpStatus.BAD_REQUEST, "자신의 역할은 변경할 수 없습니다."),
    CANNOT_DEMOTE_ADMIN("AUTH_003", HttpStatus.BAD_REQUEST, "관리자의 권한을 변경할 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
