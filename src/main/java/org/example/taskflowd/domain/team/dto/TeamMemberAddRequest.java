package org.example.taskflowd.domain.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamMemberAddRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    public TeamMemberAddRequest(Long userId) {
        this.userId = userId;
    }
}