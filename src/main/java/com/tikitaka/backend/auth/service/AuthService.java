package com.tikitaka.backend.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.tikitaka.backend.auth.dto.AuthResponse;
import com.tikitaka.backend.auth.dto.SignUpRequest;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.global.jwt.JwtProvider;
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

    public AuthResponse signUp(SignUpRequest request) {

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
            .build();
        userRepository.save(user);

        // 3. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 4. Refresh Token 저장
        // WHY: 회원가입 시점에 바로 로그인 상태로 만들어주기 위함
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
}
