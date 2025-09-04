package org.example.taskflowd.domain.user.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 반환할 사용자 정보 데이터를 담는 DTO 클래스
 *
 * 1. Service → Controller → 클라이언트로 전달되는 사용자 정보 포맷 정의
 * 2. Entity(User) 데이터를 안전하게 변환하여 외부에 노출
 *    - 예: 비밀번호는 절대 포함하지 않음
 *
 * 연관 클래스:
 * - UserService.save()/findOne()/update() 등 → Entity를 DTO로 변환 후 반환
 * - UserMapper.toResponseDto(User) → Entity → DTO 변환 헬퍼
 * - Controller → ResponseEntity<UserResponseDto>로 반환
 */

@Getter
public class UserResponseDto {
    private final Long id;
    private final String name;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    //모든 필드를 초기화하는 생성자
    public UserResponseDto(Long id, String name, String username, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
