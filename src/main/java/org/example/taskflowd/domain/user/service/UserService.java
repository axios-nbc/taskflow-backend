package org.example.taskflowd.domain.user.service;

import org.example.taskflowd.domain.user.dto.request.UserSaveRequestDto;
import org.example.taskflowd.domain.user.dto.request.UserUpdateRequestDto;
import org.example.taskflowd.domain.user.dto.request.LoginRequestDto;
import org.example.taskflowd.domain.user.dto.response.LoginResponseDto;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;

import java.util.List;

/**
 * 사용자 관련 비즈니스 로직을 정의한 서비스 계층 인터페이스
 */

public interface UserService {

    //회원가입
    UserResponseDto save(UserSaveRequestDto dto);
    //모든 사용자 조회
    List<UserResponseDto> findAll();
    //특정 사용자 프로필 조회
    UserResponseDto getProfile(Long id);
    //사용자 정보 수정
    UserResponseDto update(Long userId, UserUpdateRequestDto dto);
    //회원탈퇴
    void deleteById(Long id, String password);
    //로그인 처리 및 JWT 발급
    LoginResponseDto handleLogin(LoginRequestDto dto);
    //권한 검증
    User getUser(Long userId);

}
