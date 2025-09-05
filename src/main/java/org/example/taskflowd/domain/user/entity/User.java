package org.example.taskflowd.domain.user.entity;

import org.example.taskflowd.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 DB에 저장하는 JPA Entity 클래스
 *
 */

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String userName;

    @Column(unique = true)
    private String email;

    private String role = "USER";   // 임시 조치, enum 변환 필요

    private String password;


    // 일반 생성자
    public User(String name, String userName, String email, String password) {
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    // 사용자 정보 업데이트
    public void update(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }
}