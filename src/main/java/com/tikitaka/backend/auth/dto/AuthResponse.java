package com.tikitaka.backend.auth.dto;

// record 사용 - 응답 DTO는 불변, lombok의 getter, setter 불필요
public record AuthResponse (
    String accessToken,
    String refreshToken,
    String email,
    String role
) {}
