package org.example.taskflowd.domain.team.repository;


import org.example.taskflowd.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember,Long> {

    //특정 팀의 맴버들 조회
    List<TeamMember> findByTeamId(Long teamId);

    //특정 사용자의 팀 맴버쉽 조회
    List<TeamMember> findByUserId(Long userId);

    //특정 팀에서 특정 사용자 맴버쉽 조회
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    //특정 팀에 특정 사용자가 맴버인지 확인하기
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    // 팀 진행률 통계를 위한 집계 (팀 이름, 총 작업 수, 완료 작업 수)
    @Query("SELECT tm.team.name, COUNT(t.id), SUM(CASE WHEN t.status = 'COMPLETE' THEN 1 ELSE 0 END) " +
            "FROM TeamMember tm LEFT JOIN tm.team t2 LEFT JOIN Task t ON t.assignee.id = tm.userId AND t.deletedAt IS NULL " +
            "WHERE tm.team.id IN :teamIds GROUP BY tm.team.name")
    List<Object[]> getTeamProgressStats(@Param("teamIds") List<Long> teamIds);
}
