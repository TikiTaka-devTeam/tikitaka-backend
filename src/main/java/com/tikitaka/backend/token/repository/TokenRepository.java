package com.tikitaka.backend.token.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.token.entity.Token;
import com.tikitaka.backend.user.entity.User;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByUser(User user);

    // 로그아웃 시 또는 토큰 재발급 시 user 기준으로 토큰 삭제
    void deleteByUser(User user);
}
