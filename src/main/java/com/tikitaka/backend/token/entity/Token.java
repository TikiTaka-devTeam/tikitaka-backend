package com.tikitaka.backend.token.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token")
@Getter
@NoArgsConstructor
public class Token {

    // users와 1:1이고, user_id가 PK이자 FK로 사용되는 구조
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    // PK이자 FK이므로 @MapsId + @OneToOne 조합 사용
    // @JoinColumn만 쓰면 PK/FK 분리됨 - 여기선 동일 컬럼이어야 함
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "refresh_token", nullable = false, length = 500)
    private String refreshToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Token(User user, String refreshToken) {
        this.user = user;
        this.refreshToken = refreshToken;
    }

    // 로그인할 때마다 새 Token 객체 만들지 않고 기존 레코드의 refreshToken만 갱신하기 위한 메서드
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
