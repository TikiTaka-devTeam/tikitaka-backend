package com.tikitaka.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.LoginRequest;
import com.tikitaka.backend.auth.dto.SignUpRequest;
import com.tikitaka.backend.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일/비밀번호 기반 회원가입
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "이름, 이메일, 비밀번호, 전화번호, 대학교, 학과, 1전공, 학번, 역할(STUDENT/PROFESSOR) 필요"
    )
    public ResponseEntity<AuthResponse> signUp(
        @RequestBody @Valid SignUpRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.signUp(request));
    }

    // 이메일/비밀번호 기반 로그인
    @PostMapping("/login")
    @Operation(
        summary = "로그인"
    )
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
    @Operation(
        summary = "Access Token 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급"
    )
    public ResponseEntity<AuthResponse> reissue(
        @RequestHeader("Authorization") String authHeader
    ) {
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    @Operation(
        summary = "이메일 중복 확인",
        description = "회원가입 시 이메일이 이미 존재하는지 확인"
    )
    public ResponseEntity<Boolean> checkEmailDuplicate(
        @RequestParam String email
    ) {
        return ResponseEntity.ok(authService.checkEmailDuplicate(email));
    }
}
