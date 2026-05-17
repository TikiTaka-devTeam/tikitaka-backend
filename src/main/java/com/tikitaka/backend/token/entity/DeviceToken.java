package com.tikitaka.backend.token.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;
 
@Entity
@Table(name = "device_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeviceToken {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    // FCM에서 발급받은 기기 토큰
    @Column(name = "device_token", columnDefinition = "TEXT", nullable = false)
    private String deviceToken;
 
    // ANDROID | IOS | WEB
    @Column(name = "device_type", length = 20, nullable = false)
    private String deviceType;
 
    // 현재 유효한 토큰 여부 (로그아웃 시 FALSE 처리)
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
 
   @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
}
