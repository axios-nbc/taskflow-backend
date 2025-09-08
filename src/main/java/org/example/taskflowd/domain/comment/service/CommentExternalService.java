package org.example.taskflowd.domain.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.annotation.ActivityLogger;
import org.example.taskflowd.domain.activityLog.enums.ActLogEnum;
import org.example.taskflowd.domain.comment.dto.response.CommentListItemResponse;
import org.example.taskflowd.domain.comment.dto.response.CreateCommentResponse;
import org.example.taskflowd.domain.comment.dto.response.UpdateCommentResponse;
import org.example.taskflowd.domain.comment.exception.CommentErrorCode;
import org.example.taskflowd.domain.comment.exception.InvalidCommentException;
import org.example.taskflowd.domain.comment.mapper.CommentMapper;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.exception.InvalidTaskException;
import org.example.taskflowd.domain.task.service.TaskInternalService;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.comment.dto.request.CreateCommentRequest;
import org.example.taskflowd.domain.comment.dto.request.UpdateCommentRequest;
import org.example.taskflowd.domain.comment.dto.response.CommentResponse;
import org.example.taskflowd.domain.comment.entity.Comment;
import org.example.taskflowd.domain.comment.repository.CommentRepository;
import org.example.taskflowd.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentExternalService {
    // In domain
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    // Out of Domain
    private final UserService userService;
    private final TaskInternalService taskInternalService;

    /* ========== Helper Method ========== */
    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new InvalidTaskException(CommentErrorCode.CMT_SEARCH_FAILED_INVALID_ID));
    }
    private <T> T getCommentMapped(Long id, Function<Comment, T> mapper) {
        Comment comment = getCommentOrThrow(id);

        return mapper.apply(comment);
    }

    /* ========== Main Method ========== */
    // 3.1 Comment 생성
    @Transactional
    @ActivityLogger(type = ActLogEnum.COMMENT_CREATED, paramIndex = 1)
    public CreateCommentResponse createComment(CreateCommentRequest createCommentRequest, Long taskId, Long loginUserId) {
        // 작업 및 작성자
        Task targetTask = taskInternalService.getTaskByIdOrThrow(taskId);
        User writer = userService.getUser(loginUserId);
        Comment parent = createCommentRequest.parentId() != null ?
                getCommentOrThrow(loginUserId) : null;

        Comment comment = parent == null ?
                commentMapper.toEntity(createCommentRequest, writer, targetTask) :
                commentMapper.toEntity(createCommentRequest, writer, targetTask, parent);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCreateResponse(savedComment);
    }

    // 3.2 Task의 Comment 목록 조회
    public Page<CommentListItemResponse> getCommentsFromTask(Pageable pageable, Long taskId) {
        Task targetTask = taskInternalService.getTaskByIdOrThrow(taskId);

        Page<Comment> comments = commentRepository.findByTaskIdAndDeletedAtIsNull(taskId, pageable);

        return comments.map(commentMapper::toListItemResponse);
    }

    // 3.3 Comment 수정
    @Transactional
    @ActivityLogger(type = ActLogEnum.COMMENT_UPDATED, paramIndex = 3)
    public UpdateCommentResponse updateComment(UpdateCommentRequest updateCommentRequest, Long commentId, Long loginUserId, Long taskId) {
        User loginUser = userService.getUser(loginUserId);
        Comment comment = getCommentOrThrow(commentId);

        // 현재 로직 : 작성자만 수정가능
        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new InvalidCommentException(CommentErrorCode.CMT_IS_NOT_WRITER);
        }

        comment.updateComment(updateCommentRequest.content());

        return commentMapper.toUpdateResponse(comment);
    }

    @Transactional
    @ActivityLogger(type = ActLogEnum.COMMENT_DELETED, paramIndex = 2)
    public void deleteComment(Long commentId, Long loginUserId, Long taskId) {
        User loginUser = userService.getUser(loginUserId);
        Comment comment = getCommentOrThrow(commentId);

        // 현재 로직 : 작성자만 삭제가능
        if (!comment.getWriter().getId().equals(loginUser.getId())) {
            throw new InvalidCommentException(CommentErrorCode.CMT_IS_NOT_WRITER);
        }

        comment.delete();
    }
}
