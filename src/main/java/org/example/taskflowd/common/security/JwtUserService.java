package org.example.taskflowd.common.security;

import lombok.RequiredArgsConstructor;
import org.example.taskflowd.domain.user.entity.User;
import org.example.taskflowd.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtUserService {

    private final UserRepository userRepository;

    public AuthUser findUserById(Long userId) {
        User user = userRepository.findById(userId).get();

        return AuthUser.from(user);
    }
}
