package com.tikitaka.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.LoginRequest;
import com.tikitaka.backend.auth.dto.SignUpRequest;
import com.tikitaka.backend.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일/비밀번호 기반 회원가입
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
        @RequestBody @Valid SignUpRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.signUp(request));
    }

    // 이메일/비밀번호 기반 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestHeader("Authorization") String authHeader
    ) {
        //"Bearer {token}" 형식에서 토큰만 추출
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok().build();
    }

    // Access Token 재발급
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(
        @RequestHeader("Authorization") String authHeader
    ) {
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}
