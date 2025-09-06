package org.example.taskflowd.domain.user.repository;

import org.example.taskflowd.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

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

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND (" +
        "LOWER(u.userName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
        "LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))" )
    List<User> searchUsers(@Param("q") String q, Pageable pageable);

}
