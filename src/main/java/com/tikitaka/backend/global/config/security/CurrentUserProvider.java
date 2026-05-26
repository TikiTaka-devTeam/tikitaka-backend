package com.tikitaka.backend.global.config.security;

import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String userId = authentication.getName();

        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }
}