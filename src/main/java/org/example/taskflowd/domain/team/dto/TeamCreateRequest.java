package org.example.taskflowd.domain.team.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamCreateRequest {

    @NotBlank(message = "팀 이름은 필수입니다.")
    private String name;

    private String description;

    public TeamCreateRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}