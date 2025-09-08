package org.example.taskflowd.domain.team.service;



import lombok.RequiredArgsConstructor;
import org.example.taskflowd.common.exception.GlobalException;
import org.example.taskflowd.domain.team.dto.TeamMemberAddRequest;
import org.example.taskflowd.domain.team.dto.TeamResponse;
import org.example.taskflowd.domain.team.entity.Team;
import org.example.taskflowd.domain.team.entity.TeamMember;
import org.example.taskflowd.domain.team.exeption.TeamErrorCode;
import org.example.taskflowd.domain.team.repository.TeamMemberRepository;
import org.example.taskflowd.domain.team.repository.TeamRepository;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;

    // 팀 멤버 추가
    @Transactional
    public TeamResponse addMember(Long teamId, TeamMemberAddRequest request) {
        Team team = findTeamById(teamId);

        User user = userService.getUser(request.getUserId());

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new GlobalException(TeamErrorCode.MEMBER_ALREADY_EXISTS);
        }

        TeamMember teamMember = new TeamMember(team, request.getUserId(), "MEMBER"); // 기본 역할
        teamMemberRepository.save(teamMember);

        return convertToTeamResponse(team);
    }

    @Transactional
    public TeamResponse removeMember(Long teamId, Long userId) {
        Team team = findTeamById(teamId);

        // 팀 멤버 존재 확인
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new GlobalException(TeamErrorCode.MEMBER_NOT_FOUND));
        team.removeMember(teamMember);
        teamMember.delete();//삭제표시로 요청
        teamMemberRepository.save(teamMember); //삭제한것으로 해달라고 부탁
        return convertToTeamResponse(team);
    }

    // 추가 가능한 사용자 목록 조회
    public List<UserResponseDto> getAvailableUsers(Long teamId) {
        findTeamById(teamId);

        List<UserResponseDto> allUsers = userService.findAll();
        List<Long> teamMemberIds = teamMemberRepository.findByTeamId(teamId)
                .stream().map(TeamMember::getUserId).toList();

        return allUsers.stream()
                .filter(userDto -> !teamMemberIds.contains(userDto.id()))
                .map(userDto -> new UserResponseDto(
                        userDto.id(),
                        userDto.name(),
                        userDto.username(),
                        userDto.email(),
                        "USER",
                        userDto.createdAt()
                ))
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
