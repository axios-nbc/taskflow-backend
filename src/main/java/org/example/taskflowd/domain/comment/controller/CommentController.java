package org.example.taskflowd.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.dto.response.ApiPageResponse;
import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.domain.comment.dto.request.CreateCommentRequest;
import org.example.taskflowd.domain.comment.dto.request.UpdateCommentRequest;
import org.example.taskflowd.domain.comment.dto.response.CommentListItemResponse;
import org.example.taskflowd.domain.comment.dto.response.CreateCommentResponse;
import org.example.taskflowd.domain.comment.dto.response.UpdateCommentResponse;
import org.example.taskflowd.domain.comment.entity.Comment;
import org.example.taskflowd.domain.comment.enums.CommentResponseMessage;
import org.example.taskflowd.domain.comment.service.CommentExternalService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public class CommentController {
    // In Domain
    private final CommentExternalService commentExternalService;

    /* ========== Main Method ========== */
    // <<< /tasks >>>
    // 3.1 Comment 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @Validated @RequestBody CreateCommentRequest createCommentRequest,
            @PathVariable Long taskId) {
        return ApiResponse.created(
                CommentResponseMessage.COMMENT_CREATED,
                commentExternalService.createComment(createCommentRequest, taskId, loginUserId),
                null
        );
    }


    // 3.2 Task의 Comment 목록 조회
    @GetMapping
    public ResponseEntity<ApiPageResponse<CommentListItemResponse>> getCommentsFromTask(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @PathVariable Long taskId
    ) {
        Pageable pageable = PageRequest.of(
                page, size,
                sort.equals("oldest") ?
                        Sort.sort(Comment.class).by(Comment::getCreatedAt).ascending() :
                        Sort.sort(Comment.class).by(Comment::getCreatedAt).descending()
        );

        return ApiPageResponse.success(commentExternalService.getCommentsFromTask(pageable, taskId));
    }

    // 3.3 Comment 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<UpdateCommentResponse>> updateComment(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest updateCommentRequest) {
        return ApiResponse.ok(commentExternalService.updateComment(updateCommentRequest, commentId, loginUserId));
    }

    // 3.4 Comment 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Object>> deleteComment(
            @SessionAttribute(value = "LOGIN_USER_ID") Long loginUserId,
            @PathVariable Long commentId
    ) {
        commentExternalService.deleteComment(commentId, loginUserId);
        return ApiResponse.ok(null);
    }
}
