package org.example.taskflowd.domain.comment.mapper;

import org.example.taskflowd.domain.comment.dto.request.CreateCommentRequest;
import org.example.taskflowd.domain.comment.dto.response.CommentListItemResponse;
import org.example.taskflowd.domain.comment.dto.response.CreateCommentResponse;
import org.example.taskflowd.domain.comment.dto.response.UpdateCommentResponse;
import org.example.taskflowd.domain.comment.entity.Comment;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.example.taskflowd.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CommentMapper {
    public Comment toEntity(CreateCommentRequest request, User writer, Task task) {
        return Comment.rootBuilder().
                content(request.content()).
                writer(writer).
                task(task).
                build();
    }
    public Comment toEntity(CreateCommentRequest request, User writer, Task task, Comment parent) {
        return Comment.replyBuilder().
                content(request.content()).
                writer(writer).
                task(task).
                parent(parent).
                build();
    }

    public CreateCommentResponse toCreateResponse(Comment comment) {
        return CreateCommentResponse.toDto(
                comment.getId(), comment.getContent(), comment.getTask().getId(),
                comment.getWriter().getId(), UserMapper.toResponseDto(comment.getWriter()),
                (!Objects.isNull(comment.getParent())) ? comment.getParent().getId() : null,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }

    public CommentListItemResponse toListItemResponse(Comment comment) {
        return CommentListItemResponse.toDto(
                comment.getId(), comment.getContent(), comment.getTask().getId(),
                comment.getWriter().getId(), UserMapper.toResponseDto(comment.getWriter()),
                (!Objects.isNull(comment.getParent())) ? comment.getParent().getId() : null,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }

    public UpdateCommentResponse toUpdateResponse(Comment comment) {
        return UpdateCommentResponse.toDto(
                comment.getId(), comment.getContent(), comment.getTask().getId(),
                comment.getWriter().getId(), UserMapper.toResponseDto(comment.getWriter()),
                (!Objects.isNull(comment.getParent())) ? comment.getParent().getId() : null,
                comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
