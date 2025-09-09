package org.example.taskflowd.domain.task.repository;

import jakarta.persistence.EntityManager;
import org.example.taskflowd.common.config.JpaAuditingConfig;
import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.example.taskflowd.domain.task.repository.TaskRepository;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.temporal.ChronoUnit;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


// import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
public class TaskRepositoryTest {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EntityManager em;

    private User userA;
    private User userB;
    private User userC;

    /* ========== Init ========== */
    @BeforeEach
    void setup() {
        userA = userRepository.save(new User("USER A", "nbcA123", "a@example.com", "123456"));
        userB = userRepository.save(new User("USER B", "nbcB456", "b@example.com", "55AA55"));
        userC = userRepository.save(new User("USER C", "nbcC789", "c@example.com", "00ffff"));
    }

    /* ========== Helper Method ========== */
    private Task buildTask(
            String title, String description, User writer, User assignee,
            TaskStatus status, TaskPriority priority, LocalDateTime dueDate
    ) {
        return Task.builder()
                .title(title)
                .description(description)
                .writer(writer)
                .assignee(assignee)
                .status(status)
                .priority(priority)
                .dueDate(dueDate)
                .build();
    }

    private void reload() {
        em.flush();
        em.clear();
    }
    private Task saveAndReload(Task task) {
        Task saved = taskRepository.save(task);
        reload();
        return taskRepository.findById(saved.getId()).orElseThrow();
    }

    /* ========== CRUD ========== */
    @Test
    @DisplayName("CRUD: CREATE (with full arguments)")
    void shouldCreateTask_whenAllArgumentsProvided() {
        // given
        LocalDateTime due = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Task toSave = buildTask("제목", "내용", userA, userA, TaskStatus.TODO, TaskPriority.MEDIUM, due);

        // when
        Task found = saveAndReload(toSave);

        // then
        assertThat(found.getTitle()).isEqualTo("제목");
        assertThat(found.getAssignee().getName()).isEqualTo("USER A");
        assertThat(found.getDueDate()).isEqualTo(due);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("CRUD: CREATE (with partial arguments)")
    void shouldCreateTask_whenPartialArgumentsProvided() {
        // given
        // status/priority/dueDate null로 전달 시 기본값 확인
        Task toSave = buildTask("제목", "내용", userA, userB, null, null, null);

        // when
        Task found = saveAndReload(toSave);

        // then
        assertThat(found.getTitle()).isEqualTo("제목");
        assertThat(found.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(found.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(found.getDueDate()).isNull();
    }

    @Test
    @DisplayName("CRUD: UPDATE")
    void shouldUpdateTaskAndTouchUpdatedAt_whenFieldsChanged() {
        // given
        Task origin = saveAndReload(buildTask(
                "이전 제목", "이전 내용", userA, userB,
                TaskStatus.TODO, TaskPriority.MEDIUM, null));

        // when
        LocalDateTime due = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Task target = taskRepository.findById(origin.getId()).orElseThrow();
        target.updateTask("새 제목", "새 내용", due, userC);
        target.updateStatus(TaskStatus.IN_PROGRESS);
        target.updatePriority(TaskPriority.LOW);
        reload();
        Task found = taskRepository.findById(origin.getId()).orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("새 제목");
        assertThat(found.getDescription()).isEqualTo("새 내용");
        assertThat(found.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(found.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(found.getAssignee().getName()).isEqualTo("USER C");
        assertThat(found.getDueDate()).isEqualTo(due);
        assertThat(found.getCreatedAt()).isNotEqualTo(found.getUpdatedAt());
    }

    @Test
    @DisplayName("CRUD: DELETE")
    void shouldSoftDeleteTask_whenDeleteInvoked() {
        // given
        Task saved = saveAndReload(buildTask(
                "제목", "내용", userA, userA,
                TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now()));

        // when
        Task target = taskRepository.findById(saved.getId()).orElseThrow();
        target.delete();
        reload();

        Task found = taskRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getDeletedAt()).isNotNull();
    }

    /* ========== Method ========== */
    @Test
    @DisplayName("METHOD: findByAssigneeID - valid list")
    void findByAssigneeID_shouldReturnPagedTasks_whenGiveValidAssignee() {
        // given
        // writer A Assignee B 7개, writer B Assignee B 4개, writer B Assignee C 5개 | Assignee B 탐색
        IntStream.rangeClosed(1, 7)
                .forEach(i -> taskRepository.save(buildTask(
                        "Title-A-"+i,
                        "Description-A-"+i,
                        userA,
                        userB,
                        null,null,null)));

        IntStream.rangeClosed(1, 4)
                .forEach(i -> taskRepository.save(buildTask(
                        "Title-B-"+i,
                        "Description-B-"+i,
                        userB,
                        userB,
                        null,null,null)));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> taskRepository.save(buildTask(
                        "Title-C-"+i,
                        "Description-C-"+i,
                        userB,
                        userC,
                        null,null,null)));
        reload();



        // when
        // page 0 size 5
        Pageable page0 = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Task> result0 = taskRepository.findByAssigneeId(userB.getId(), page0);

        // then
        assertThat(result0.getNumber()).isEqualTo(0);
        assertThat(result0.getSize()).isEqualTo(5);
        assertThat(result0.getTotalElements()).isEqualTo(11);
        assertThat(result0.getTotalPages()).isEqualTo(3);
        assertThat(result0.getContent()).hasSize(5);
        assertThat(result0.getContent()).allMatch(t -> t.getAssignee().getId().equals(userB.getId()));

        // when
        // page 1 size 5
        Pageable page2 = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Task> result2 = taskRepository.findByAssigneeId(userB.getId(), page2);

        // then
        assertThat(result2.getNumber()).isEqualTo(2);
        assertThat(result2.getContent()).hasSize(1);
        assertThat(result2.getContent()).allMatch(t -> t.getAssignee().getId().equals(userB.getId()));
    }

    @Test
    @DisplayName("METHOD: findByAssigneeID - invalid list")
    void findByAssigneeID_shouldReturnEmptyPagedTasks_whenGiveInvalidAssignee() {
        // given
        // writer A Assignee B 7개 | Assignee C 탐색
        IntStream.rangeClosed(1, 7)
                .forEach(i -> taskRepository.save(buildTask(
                        "Title-A-"+i,
                        "Description-A-"+i,
                        userA,
                        userB,
                        null,null,null)));
        reload();

        // when
        // page 0 size 5 id desc
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Task> result = taskRepository.findByAssigneeId(userC.getId(), pageable);

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalPages()).isZero();
    }

    /* ========== Specification ========== */
}
