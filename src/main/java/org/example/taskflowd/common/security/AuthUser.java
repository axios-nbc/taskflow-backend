package org.example.taskflowd.common.security;

import org.example.taskflowd.domain.user.entity.User;

public record AuthUser(
        Long id,
        String username,
        String role) {

    public static AuthUser from(User user) {
        return new AuthUser(
                user.getId(),
                user.getUserName(),
                user.getRole()
        );
    }
}
