package org.example.taskflowd.domain.user.dto.mapper;

import org.example.taskflowd.domain.user.dto.response.UserResponseDto;
import org.example.taskflowd.domain.user.entity.User;



/**
 * User Entity와 UserResponseDto 간 변환을 담당하는 클래스
 *
 * 1. Entity → DTO 변환
 * 2. 비밀번호 등 민감 정보는 DTO에 포함하지 않고 외부 노출 방지
 * 3. 코드 중복 최소화
 */

public class UserMapper {
    public static UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
