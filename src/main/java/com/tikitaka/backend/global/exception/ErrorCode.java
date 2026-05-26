package com.tikitaka.backend.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다"),
    SLIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "슬라이드를 찾을 수 없습니다"),
    PRIVATE_STROKE_NOT_FOUND(HttpStatus.NOT_FOUND, "개인 스트로크를 찾을 수 없습니다"),
    SHARED_STROKE_NOT_FOUND(HttpStatus.NOT_FOUND, "공유 스트로크를 찾을 수 없습니다"),
    STROKE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "스트로크 접근 권한이 없습니다"),
    PROFESSOR_ONLY(HttpStatus.FORBIDDEN, "교수님만 접근 가능합니다");

    private final HttpStatus status;
    private final String message;
}