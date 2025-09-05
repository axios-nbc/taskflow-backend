package org.example.taskflowd.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일이 존재하지 않습니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 이메일은 이미 사용중입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 닉네임은 이미 사용중입니다."),
    USER_DELETED(HttpStatus.GONE, "해당 사용자는 존재하지 않습니다."),

    // DASHBOARD
    DASHBOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "대시보드에 접근할 권한이 없습니다."),
    DASHBOARD_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "대시보드 데이터를 찾을 수 없습니다."),

    // SEARCH
    SEARCH_QUERY_EMPTY(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    SEARCH_QUERY_TOO_SHORT(HttpStatus.BAD_REQUEST, "검색어는 최소 2글자 이상 입력해주세요."),
    SEARCH_RESULTS_EMPTY(HttpStatus.NOT_FOUND, "검색 결과가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}