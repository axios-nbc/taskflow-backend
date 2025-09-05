package org.example.taskflowd.domain.user.repository;

import org.example.taskflowd.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Entity의 데이터 접근을 담당하는 JPA Repository 인터페이스
 * - User : Entity와 매핑
 * - UserService : 비즈니스 로직에서 Repository 호출
 */

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNameAndDeletedAtIsNull(String userName);
    //이메일 중복 체크
    boolean existsByEmail(String email);
    //사용자 이름 중복 체크
    boolean existsByUserName(String userName);

}
