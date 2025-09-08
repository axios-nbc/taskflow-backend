package org.example.taskflowd.domain.team.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 단위 테스트")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private TeamMember teamMember;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        // 테스트용 팀 생성
        team = new Team("개발팀", "백엔드 개발팀입니다");

        // 리플렉션을 사용해 ID 설정 (테스트용)
        setFieldValue(team, "id", 1L);
        // createdAt 설정 제거 - TeamResponse에서 null이어도 문제없음

        // 테스트용 팀 멤버 생성
        teamMember = new TeamMember(team, 1L, "MEMBER");

        // 테스트용 사용자 DTO 생성
        userResponseDto = new UserResponseDto(
                1L,
                "홍길동",
                "hong",
                "hong@example.com",
                "MEMBER",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("모든 팀 조회 - 성공")
    void getAllTeams_Success() {
        // given
        List<Team> teams = Arrays.asList(team);
        given(teamRepository.findAll()).willReturn(teams);
        given(teamMemberRepository.findByTeamId(1L)).willReturn(Arrays.asList(teamMember));
        given(userService.getProfile(1L)).willReturn(userResponseDto);

        // when
        List<TeamResponse> result = teamService.getAllTeams();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("개발팀");
        assertThat(result.get(0).getDescription()).isEqualTo("백엔드 개발팀입니다");
        verify(teamRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("특정 팀 조회 - 성공")
    void getTeamById_Success() {
        // given
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamId(1L)).willReturn(Arrays.asList(teamMember));
        given(userService.getProfile(1L)).willReturn(userResponseDto);

        // when
        TeamResponse result = teamService.getTeamById(1L);

        // then
        assertThat(result.getName()).isEqualTo("개발팀");
        assertThat(result.getDescription()).isEqualTo("백엔드 개발팀입니다");
        assertThat(result.getMembers()).hasSize(1);
        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("특정 팀 조회 - 실패 (팀이 존재하지 않음)")
    void getTeamById_NotFound() {
        // given
        given(teamRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getTeamById(999L))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", TeamErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("팀 생성 - 성공")
    void createTeam_Success() {
        // given
        TeamCreateRequest request = new TeamCreateRequest("개발팀", "백엔드 개발팀입니다");
        given(teamRepository.existsByName("개발팀")).willReturn(false);
        given(teamRepository.save(any(Team.class))).willReturn(team);
        given(teamMemberRepository.findByTeamId(1L)).willReturn(List.of());

        // when
        TeamResponse result = teamService.createTeam(request);

        // then
        assertThat(result.getName()).isEqualTo("개발팀");
        assertThat(result.getDescription()).isEqualTo("백엔드 개발팀입니다");
        verify(teamRepository, times(1)).existsByName("개발팀");
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("팀 생성 - 실패 (중복된 팀 이름)")
    void createTeam_DuplicateName() {
        // given
        TeamCreateRequest request = new TeamCreateRequest("개발팀", "백엔드 개발팀입니다");
        given(teamRepository.existsByName("개발팀")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", TeamErrorCode.TEAM_NAME_DUPLICATE);
    }

    @Test
    @DisplayName("팀 정보 수정 - 성공")
    void updateTeam_Success() {
        // given
        TeamUpdateRequest request = new TeamUpdateRequest("디자인팀", "UI/UX 디자인팀입니다");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByName("디자인팀")).willReturn(false);
        given(teamMemberRepository.findByTeamId(1L)).willReturn(Arrays.asList(teamMember));
        given(userService.getProfile(1L)).willReturn(userResponseDto);

        // when
        TeamResponse result = teamService.updateTeam(1L, request);

        // then
        assertThat(result.getName()).isEqualTo("디자인팀");
        assertThat(result.getDescription()).isEqualTo("UI/UX 디자인팀입니다");
        verify(teamRepository, times(1)).findById(1L);
        verify(teamRepository, times(1)).existsByName("디자인팀");
    }

    @Test
    @DisplayName("팀 정보 수정 - 실패 (중복된 팀 이름)")
    void updateTeam_DuplicateName() {
        // given
        TeamUpdateRequest request = new TeamUpdateRequest("디자인팀", "UI/UX 디자인팀입니다");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByName("디자인팀")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> teamService.updateTeam(1L, request))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", TeamErrorCode.TEAM_NAME_DUPLICATE);
    }

    @Test
    @DisplayName("팀 삭제 - 성공")
    void deleteTeam_Success() {
        // given
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        // when
        teamService.deleteTeam(1L);

        // then
        verify(teamRepository, times(1)).findById(1L);
        // Team은 실제 객체이므로 verify 불가, 호출만 확인
    }

    @Test
    @DisplayName("팀 멤버 목록 조회 - 성공")
    void getTeamMembers_Success() {
        // given
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.findByTeamId(1L)).willReturn(Arrays.asList(teamMember));
        given(userService.getProfile(1L)).willReturn(userResponseDto);

        // when
        List<UserResponseDto> result = teamService.getTeamMembers(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("홍길동");
        assertThat(result.get(0).email()).isEqualTo("hong@example.com");
        verify(teamRepository, times(1)).findById(1L);
        verify(teamMemberRepository, times(1)).findByTeamId(1L);
    }

    // 리플렉션을 사용한 필드 설정 헬퍼 메서드
    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}