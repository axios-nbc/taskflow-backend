package org.example.taskflowd.domain.team.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.taskflowd.common.entity.BaseEntity;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(TeamMemberId.class)
public class TeamMember extends BaseEntity {

    @Id
    private Long id;

    @Id
    private UUID idPk;

    private UUID idPk2;

    private Long userId;

    private String role;

    // 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @Setter
    private Team team;

    // 생성자
    public TeamMember(Long id, UUID idPk, Long userId, String role) {
        this.id = id;
        this.idPk = idPk;
        this.userId = userId;
        this.role = role;
    }

    // 편의 생성자
    public TeamMember(Team team, Long userId, String role) {
        this.id = team.getId();
        this.idPk = team.getIdPk(); // Team의 UUID 사용
        this.userId = userId;
        this.role = role;
        this.team = team;
    }

    // 비즈니스 메서드
    public void updateRole(String role) {
        if (role != null && !role.trim().isEmpty()) {
            this.role = role;
        }
    }
}