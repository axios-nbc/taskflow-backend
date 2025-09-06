package org.example.taskflowd.domain.team.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.taskflowd.domain.user.dto.response.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<UserResponseDto> members;

    public TeamResponse(Long id, String name, String description,
                        LocalDateTime createdAt, List<UserResponseDto> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.members = members;
    }
}
