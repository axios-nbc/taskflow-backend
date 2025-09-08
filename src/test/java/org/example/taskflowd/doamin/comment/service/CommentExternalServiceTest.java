package org.example.taskflowd.doamin.comment.service;

import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.example.taskflowd.domain.comment.dto.request.CreateCommentRequest;
import org.example.taskflowd.domain.comment.dto.request.UpdateCommentRequest;
import org.example.taskflowd.domain.comment.dto.response.CommentResponse;
import org.example.taskflowd.domain.comment.entity.Comment;
import org.example.taskflowd.domain.comment.repository.CommentRepository;
import org.example.taskflowd.domain.comment.service.CommentExternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CommentExternalServiceTest {

    @InjectMocks
    private CommentExternalService commentExternalService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;

    @Test
    @DisplayName("[CommentService - createdComment] 정상 작동 확인")
    @Transactional
    void createComment() {

        // given
        CreateCommentRequest createCommentRequest = new CreateCommentRequest("내용", null);
        User user = new User();
        Task task = new Task("title", "description", user, user, TaskStatus.TODO, TaskPriority.HIGH, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(task, "id", 2L);
        given(taskRepository.findById(anyLong())).willReturn(Optional.of(task));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        Comment savedComment = Comment.create(createCommentRequest, task, user);
        ReflectionTestUtils.setField(savedComment, "id", 1L);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse result = commentExternalService.createComment(createCommentRequest, 2L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(savedComment.getId());
        assertThat(result.taskId()).isEqualTo(task.getId());
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.content()).isEqualTo(createCommentRequest.content());
        assertThat(result.parentId()).isNull();
    }

    @Test
    @DisplayName("[CommentService - updateComment] 정상 작동 확인")
    @Transactional
    void updateComment() {

        // given
        CreateCommentRequest createCommentRequest = new CreateCommentRequest("내용", null);
        User user = new User();
        Task task = new Task("title", "description", user, user, TaskStatus.TODO, TaskPriority.HIGH, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(task, "id", 2L);
        Comment comment = Comment.create(createCommentRequest, task, user);

        UpdateCommentRequest updateCommentRequest = new UpdateCommentRequest("업데이트");
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(comment));

        // when
        CommentResponse result = commentExternalService.updateComment(updateCommentRequest, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(comment.getId());
        assertThat(result.taskId()).isEqualTo(task.getId());
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.content()).isEqualTo(updateCommentRequest.content());
        assertThat(result.parentId()).isNull();
    }
}
