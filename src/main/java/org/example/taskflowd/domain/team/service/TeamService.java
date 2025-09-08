package org.example.taskflowd.domain.team.service;


import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.exception.GlobalException;
import org.example.taskflowd.domain.team.dto.TeamCreateRequest;
import org.example.taskflowd.domain.team.dto.TeamResponse;
import org.example.taskflowd.domain.team.dto.TeamUpdateRequest;
import org.example.taskflowd.domain.team.entity.Team;
import org.example.taskflowd.domain.team.entity.TeamMember;
import org.example.taskflowd.domain.team.exeption.TeamErrorCode;
import org.example.taskflowd.domain.team.repository.TeamMemberRepository;
import org.example.taskflowd.domain.team.repository.TeamRepository;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;
    //팀 목록 조회
    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return teams.stream()
                .map(this::convertToTeamResponse)
                .collect(Collectors.toList());
    }

    // 특정 팀 조회
    public TeamResponse getTeamById(Long teamId) {
        Team team = findTeamById(teamId);
        return convertToTeamResponse(team);
    }

    // 팀 생성
    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        // 팀 이름 중복 체크
        if (teamRepository.existsByName(request.getName())) {
            throw new GlobalException(TeamErrorCode.TEAM_NAME_DUPLICATE);
        }

        Team team = new Team(request.getName(), request.getDescription());
        Team savedTeam = teamRepository.save(team);

        return convertToTeamResponse(savedTeam);
    }

    // 팀 정보 수정
    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamUpdateRequest request) {
        Team team = findTeamById(teamId);

        // 이름이 변경되었고, 다른 팀에서 사용 중인 이름인지 체크
        if (!team.getName().equals(request.getName()) &&
                teamRepository.existsByName(request.getName())) {
            throw new GlobalException(TeamErrorCode.TEAM_NAME_DUPLICATE);
        }

        team.updateTeamInfo(request.getName(), request.getDescription());
        return convertToTeamResponse(team);
    }

    // 팀 삭제
    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = findTeamById(teamId);
        team.delete(); // BaseEntity의 soft delete
    }

    // 팀 멤버 목록 조회
    public List<UserResponseDto> getTeamMembers(Long teamId) {
        // 팀 존재 확인
        findTeamById(teamId);

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        return members.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    // Entity to DTO 변환 메서드
    private TeamResponse convertToTeamResponse(Team team) {
        List<UserResponseDto> members = teamMemberRepository.findByTeamId(team.getId())
                .stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt(),
                members
        );
    }

    private UserResponseDto convertToUserResponse(TeamMember teamMember) {
        UserResponseDto userDto = userService.getProfile(teamMember.getUserId());
        return new UserResponseDto(
                userDto.id(),
                userDto.name(),
                userDto.username(),
                userDto.email(),
                teamMember.getRole(),
                userDto.createdAt()
        );
    }

    // 공통 메서드
    private Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(TeamErrorCode.TEAM_NOT_FOUND));
    }
}