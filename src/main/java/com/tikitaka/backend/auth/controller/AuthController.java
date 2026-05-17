package com.tikitaka.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitaka.backend.auth.dto.AuthResponse;
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
}
