package org.example.taskflowd.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.example.taskflowd.common.enums.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDateTime;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(String loginSuccess, String token) {

        this.success = true;
        this.message = loginSuccess;             
        this.data = (T) token;
        this.timestamp = LocalDateTime.now();

    }

    /* ---------- Factory Method ---------- */
    public static <T> ApiResponse<T> ofSuccess(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(true, "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> ofSuccessMessage(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> ofSuccess(ResponseMessage message, T data) {
        return new ApiResponse<>(true, message.getMessage(), data);
    }

    public static <T> ApiResponse<T> ofSuccess(ResponseMessage message) {
        return new ApiResponse<>(true, message.getMessage(), null);
    }

    /* ---------- ResponseEntity ---------- */
    //  201 Created + (선택) Location 헤더 + body
    public static <T> ResponseEntity<ApiResponse<T>> created(ResponseMessage msg, T data, URI location) {
        ApiResponse<T> body = ofSuccess(msg, data);
        return (location != null)
                ? ResponseEntity.created(location).body(body)
                : ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // 200 OK + body
    public static <T> ResponseEntity<ApiResponse<T>> ok(ResponseMessage msg, T data) {
        return ResponseEntity.ok(ofSuccess(msg, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String msg, T data) {
        return ResponseEntity.ok(ofSuccess(msg, data));
    }

    // 200 OK + body (데이터만)
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ofSuccess(data));
    }

    // 200 OK + body (메시지만)
    public static ResponseEntity<ApiResponse<Void>> okMessage(ResponseMessage msg) {
        return ResponseEntity.ok(ofSuccess(msg));
    }

    // 200 OK (data 없음, 204 No Content 대체)
    public static ResponseEntity<ApiResponse<Void>> okEmpty() {
        return ResponseEntity.ok(ofSuccess("요청이 성공했습니다.", null));
    }
}