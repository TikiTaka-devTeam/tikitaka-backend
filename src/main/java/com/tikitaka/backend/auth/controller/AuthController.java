package com.tikitaka.backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.LoginRequest;
import com.tikitaka.backend.auth.dto.ProfileImageResponse;
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

    @PostMapping( value = "/create-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "회원가입 시 프로필 이미지 업로드",
        description = "회원가입 전에 프로필 이미지를 업로드하고 URL을 반환"
    )
    public ResponseEntity<ProfileImageResponse> createProfileImage(
        @RequestParam("file") MultipartFile file
    ) {
        String profileUrl = authService.createProfileImage(file);
        return ResponseEntity.ok(new ProfileImageResponse(profileUrl));
    }
    
    // 프로필 이미지 수정
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "프로필 이미지 수정",
        description = "FormData의 file 필드로 프로필 이미지를 업로드하고 사용자 profile_url을 갱신"
    )
    public ResponseEntity<ProfileImageResponse> uploadProfileImage(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam("file") MultipartFile file
    ) {
        String accessToken = extractBearerToken(authHeader);
        jwtProvider.isTokenValid(accessToken);

        UUID userId = UUID.fromString(jwtProvider.extractUserId(accessToken));
        String profileUrl = authService.uploadProfileImage(userId, file);

        return ResponseEntity.ok(new ProfileImageResponse(profileUrl));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 올바르지 않습니다.");
        }

        return authHeader.substring(7);
    }
}
