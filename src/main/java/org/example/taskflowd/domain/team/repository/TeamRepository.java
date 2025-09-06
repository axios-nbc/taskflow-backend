package org.example.taskflowd.domain.team.repository;

import org.example.taskflowd.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT t FROM Team t WHERE t.deletedAt IS NULL AND (" +
        "LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
        "LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))" )
    List<Team> searchTeams(@Param("q") String q, Pageable pageable);
}