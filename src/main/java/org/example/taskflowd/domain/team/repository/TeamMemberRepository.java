package org.example.taskflowd.domain.team.repository;


import org.example.taskflowd.domain.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
