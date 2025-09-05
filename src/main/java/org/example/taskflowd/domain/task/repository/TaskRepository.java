package org.example.taskflowd.domain.task.repository;

import org.example.taskflowd.domain.task.entity.Task;
import org.example.taskflowd.domain.task.enums.TaskPriority;
import org.example.taskflowd.domain.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    // 작성자/담당자 기준 페이징
    Page<Task> findByWriterId(Long writerId, Pageable pageable);
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    // 상태/우선순위 기준 페이징
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    // 마감일 기준 조회
    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    List<Task> findByDueDateBefore(LocalDateTime before);
    List<Task> findByDueDateAfter(LocalDateTime after);

    // 고유 제약(제목+작성자) 검증/조회
    boolean existsByTitleAndWriterId(String title, Long writerId);
    Optional<Task> findByTitleAndWriterId(String title, Long writerId);

    // 권한 체크 용도
    Optional<Task> findByIdAndAssigneeId(Long id, Long assigneeId);
    Optional<Task> findByIdAndWriterId(Long id, Long writerId);

    // 삭제 필터링 id 탐색
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);

    /* ===== N+1 방지용 EntityGraph ===== */

    // 단건 상세: 작성자/담당자 즉시 로딩
    @EntityGraph(attributePaths = {"writer", "assignee"})
    Optional<Task> findWithWriterAndAssigneeById(Long id);

    // 목록: 담당자 기준 페이징
    @EntityGraph(attributePaths = {"writer", "assignee"})
    Page<Task> findByAssigneeIdOrderByDueDateAsc(Long assigneeId, Pageable pageable);

    /* ===== 정렬 조회 ===== */

    // TODO : 필터 기능 조회 및 추가

    // 담당자+상태 마감일 오름차순
    Page<Task> findByAssigneeIdAndStatusOrderByDueDateAsc(Long assigneeId, TaskStatus status, Pageable pageable);

    // DashboardService용
    @Query("SELECT t FROM Task t " +
        "WHERE t.assignee.id = :assigneeId " +
        "AND t.dueDate BETWEEN :startDate AND :endDate " +
        "AND t.deletedAt IS NULL")
    List<Task> findByAssigneeIdAndDueDateBetween(
        @Param("assigneeId") Long assigneeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Task t " +
        "WHERE t.assignee.id = :assigneeId " +
        "AND t.dueDate > :afterDate " +
        "AND t.deletedAt IS NULL " +
        "ORDER BY t.dueDate ASC")
    List<Task> findByAssigneeIdAndDueDateAfter(
        @Param("assigneeId") Long assigneeId,
        @Param("afterDate") LocalDateTime afterDate);

    @Query("SELECT t FROM Task t " +
        "WHERE t.assignee.id = :assigneeId " +
        "AND t.dueDate < :beforeDate " +
        "AND t.deletedAt IS NULL " +
        "ORDER BY t.dueDate DESC")
    List<Task> findByAssigneeIdAndDueDateBefore(
        @Param("assigneeId") Long assigneeId,
        @Param("beforeDate") LocalDateTime beforeDate);
}