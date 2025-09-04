package org.example.taskflowd.domain.user.service;


import org.example.taskflowd.common.exception.GlobalException;
import org.example.taskflowd.common.util.ErrorCodeEnum;
import org.example.taskflowd.domain.user.dto.request.UserUpdateRequestDto;
import org.example.taskflowd.domain.user.dto.mapper.UserMapper;
import org.example.taskflowd.domain.user.dto.request.UserSaveRequestDto;
import org.example.taskflowd.domain.user.dto.request.LoginRequestDto;
import org.example.taskflowd.domain.user.dto.response.LoginResponseDto;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.example.taskflowd.domain.auth.jwt.JwtProvider;

import java.util.List;
import java.util.stream.Collectors;


/**
 * UserService 인터페이스 구현체
 * 사용자 관련 비즈니스 로직 처리
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;


    // 회원가입
    @Override
    @Transactional
    public UserResponseDto save(UserSaveRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new GlobalException(ErrorCodeEnum.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = new User(dto.getName(), dto.getUsername(), dto.getEmail(), encodedPassword);
        userRepository.save(user);

        return UserMapper.toResponseDto(user);
    }

    // 로그인 → JWT 발급
    @Override
    @Transactional
    public LoginResponseDto handleLogin(LoginRequestDto dto) {
        // 1. 이메일로 사용자 조회 (Soft Delete 적용)
        User user = userRepository.findByUserNameAndDeletedAtIsNull(dto.getUsername())
                .orElseThrow(() -> new GlobalException(ErrorCodeEnum.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ErrorCodeEnum.USER_NOT_FOUND);

        }

        // 3. JWT 생성 (일관되게 userId를 subject로)
        String token = jwtProvider.createToken(String.valueOf(user.getId()));
        return new LoginResponseDto(token);
    }


    // 모든 유저 조회
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // 특정 유저 조회
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCodeEnum.USER_NOT_FOUND));
        if (user.isDeleted()) throw new GlobalException(ErrorCodeEnum.USER_DELETED);
        return UserMapper.toResponseDto(user);
    }


    // 유저 계정 내용 수정
    @Override
    @Transactional
    public UserResponseDto update(Long userId, UserUpdateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCodeEnum.USER_NOT_FOUND));

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new GlobalException(ErrorCodeEnum.EMAIL_ALREADY_EXISTS);
        }

        if (!user.getUserName().equals(dto.getUserName()) && userRepository.existsByUserName(dto.getUserName())) {
            throw new GlobalException(ErrorCodeEnum.USERNAME_ALREADY_EXISTS);
        }

        user.update(dto.getUserName(), dto.getEmail(), passwordEncoder.encode(dto.getPassword()));

        return UserMapper.toResponseDto(user);
    }


    // 회원탈퇴 (Soft Delete + 비밀번호 확인)
    @Override
    @Transactional
    public void deleteById(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCodeEnum.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GlobalException(ErrorCodeEnum.PASSWORD_NOT_MATCH); // 에러 코드 수정
        }

        // BaseEntity delete() 메서드 사용
        user.delete();
    }

    // userId 기반으로 User 객체 반환
    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCodeEnum.USER_NOT_FOUND));
    }
}


