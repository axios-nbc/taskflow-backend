package org.example.taskflowd.domain.user.controller;


import org.example.taskflowd.common.dto.response.ApiResponse;
import org.example.taskflowd.domain.user.dto.request.LoginRequestDto;
import org.example.taskflowd.domain.user.dto.response.LoginResponseDto;
import org.example.taskflowd.domain.user.service.UserService;
import org.example.taskflowd.domain.user.util.UserConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




/**
 * 로그인 및 로그아웃 API를 처리하는 컨트롤러
 * - /api/auth/login : 로그인 처리
 * - /api/auth/logout : 로그아웃 처리
 */

@RestController
@RequiredArgsConstructor // final 필드(userService)를 자동으로 생성자 주입
@RequestMapping("/api/auth")
public class LoginController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    /**
     * 로그인 API
     */

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequestDto dto)
    {   // 서비스에서 로그인 처리 및 JWT 발급
        LoginResponseDto token = userService.handleLogin(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess(UserConst.LOGIN_SUCCESS, token));
    }

    /**
     * 로그아웃 API
     *
     */

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(UserConst.LOGOUT_SUCCESS, null));
    }
}


