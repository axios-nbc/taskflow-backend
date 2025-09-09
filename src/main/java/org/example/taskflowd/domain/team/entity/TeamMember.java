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
    private Long userId;

    private String role;

    // 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @Setter
    private Team team;


    // 편의 생성자
    public TeamMember(Team team, Long userId, String role) {
        this.id = team.getId();
        this.userId = userId;
        this.role = role;
        this.team = team;
    }

}