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
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.LoginRequest;
import com.tikitaka.backend.auth.dto.ProfileImagePresignedUrlRequest;
import com.tikitaka.backend.auth.dto.ProfileImagePresignedUrlResponse;
import com.tikitaka.backend.auth.dto.ProfileImageResponse;
import com.tikitaka.backend.auth.dto.ProfileImageUpdateRequest;
import com.tikitaka.backend.auth.dto.SignUpRequest;
import com.tikitaka.backend.auth.service.AuthService;
import com.tikitaka.backend.global.jwt.JwtProvider;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

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

    @PostMapping("/create-profile-image")
    @Operation(
        summary = "회원가입 시 프로필 이미지 Presigned URL 발급",
        description = "회원가입 전에 S3 직접 업로드용 Presigned URL, object key, CloudFront URL을 반환"
    )
    public ResponseEntity<ProfileImagePresignedUrlResponse> createProfileImage(
        @RequestBody @Valid ProfileImagePresignedUrlRequest request
    ) {
        return ResponseEntity.ok(authService.createProfileImagePresignedUrl(request));
    }
    
    @PostMapping("/profile-image/presigned-url")
    @Operation(
        summary = "프로필 이미지 수정용 Presigned URL(임시 업로드 링크) 발급",
        description = "로그인 사용자의 프로필 이미지 수정을 위한 S3 직접 업로드 URL을 반환"
    )
    public ResponseEntity<ProfileImagePresignedUrlResponse> createProfileImageForUpdate(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody @Valid ProfileImagePresignedUrlRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        return ResponseEntity.ok(authService.createProfileImagePresignedUrl(request));
    }

    // 프로필 이미지 수정 확정
    @PostMapping("/profile-image")
    @Operation(
        summary = "프로필 이미지 수정 이후, 이전 이미지 삭제 요청",
        description = "S3 업로드 완료 후 object key로 사용자 profile_url을 갱신하고 이전 이미지를 삭제"
    )
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody @Valid ProfileImageUpdateRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        String profileUrl = authService.updateProfileImage(userId, request.objectKey());

        return ResponseEntity.ok(new ProfileImageResponse(profileUrl));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }

        return authHeader.substring(7);
    }
}
