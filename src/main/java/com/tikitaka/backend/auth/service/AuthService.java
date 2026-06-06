package com.tikitaka.backend.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.LoginRequest;
import com.tikitaka.backend.auth.dto.ProfileImagePresignedUrlRequest;
import com.tikitaka.backend.auth.dto.ProfileImagePresignedUrlResponse;
import com.tikitaka.backend.auth.dto.SignUpRequest;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.global.storage.S3StorageService;
import com.tikitaka.backend.token.entity.Token;
import com.tikitaka.backend.token.repository.TokenRepository;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final S3StorageService s3StorageService;

    public AuthResponse signUp(SignUpRequest request) { // 회원가입 로직

        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. User 생성
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .role(request.getRole())
            .univ(request.getUniv())
            .major(request.getMajor())
            .phoneNumber(request.getPhoneNumber())
            .memberIdNumber(request.getMemberIdNumber())
            .profileUrl(request.getProfileImageKeyOrLegacyProfileUrl()) // S3 object key 저장
            .build();
        userRepository.save(user);

        // 3. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 4. Refresh Token 저장
        // 회원가입 시점에 바로 로그인 상태로 만들어주기 위함
        Token token = Token.builder()
            .user(user)
            .refreshToken(refreshToken)
            .build();
        tokenRepository.save(token);

        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getEmail(),
            user.getRole().name()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) { // 이메일/비밀번호 로그인 로직

        // 1. 이메일로 User 조회
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        // passwordEncoder.matches()는 raw password와 암호화된 password 비교
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 4. Refresh Token 갱신
        // 로그인할 때마다 새 객체 INSERT하면 토큰 테이블이 무한 증가하므로, 기존 레코드가 있으면 UPDATE, 없으면 INSERT로 처리
        Token token = tokenRepository.findByUser(user)
            .orElse(Token.builder()
                .user(user)
                .refreshToken(refreshToken)
                .build());
        token.updateRefreshToken(refreshToken);
        tokenRepository.save(token);

        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getEmail(),
            user.getRole().name()
        );
    }

    @Transactional
    public void logout(String accessToken) { // 로그아웃 로직

        // 1. Access Token 검증 및 userId 추출
        jwtProvider.isTokenValid(accessToken);
        String userId = jwtProvider.extractUserId(accessToken);

        // 2. User 조회
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. Refresh Token 삭제
        // Refresh Token 삭제하면 재발급 불가 -> Access Token이 만료될 때까지는 로그아웃 상태 유지 가능
        tokenRepository.deleteByUser(user);
    }

    @Transactional
    public AuthResponse reissue(String refreshToken) { // Access Token 재발급 로직

        // 1. Refresh Token 유효성 검증
        jwtProvider.isTokenValid(refreshToken);
        String userId = jwtProvider.extractUserId(refreshToken);

        // 2. User 조회
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. DB의 Refresh Token과 비교
        // 탈취된 토큰으로 재발급 요청하는 것 방어
        Token token = tokenRepository.findByUser(user)
            .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_INVALID));

        if (!token.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        // 4. 새 토큰 발급 + Refresh Token Rotation
        // Rotation → 재발급할 때마다 Refresh Token도 교체
        // 탈취된 토큰이 재사용되면 불일치로 감지 가능
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);
        token.updateRefreshToken(newRefreshToken);

        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            user.getEmail(),
            user.getRole().name()
        );
    }

    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    public ProfileImagePresignedUrlResponse createProfileImagePresignedUrl(ProfileImagePresignedUrlRequest request) {
        S3StorageService.PresignedUpload upload = s3StorageService.createProfileImageUpload(
            request.originalFilename(),
            request.contentType()
        );

        return new ProfileImagePresignedUrlResponse(
            upload.uploadUrl(),
            upload.objectKey(),
            upload.profileUrl(),
            upload.expiresInSeconds()
        );
    }

    public String updateProfileImage(UUID userId, String objectKey) {
        s3StorageService.validateProfileObjectKey(objectKey);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String previousObjectKey = user.getProfileUrl();
        user.updateProfileUrl(objectKey);

        if (previousObjectKey != null && !previousObjectKey.equals(objectKey)) {
            s3StorageService.deleteObjectIfExists(previousObjectKey);
        }

        return s3StorageService.toCloudFrontUrl(objectKey);
    }
}
