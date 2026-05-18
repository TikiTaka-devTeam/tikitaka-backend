package com.tikitaka.backend.layer.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "private_layers",
    uniqueConstraints = {
        // 슬라이드당 사용자 1개의 개인 레이어만 허용
        @UniqueConstraint(columnNames = {"slide_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PrivateLayer {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // 학생: 본인 필기용 / 교수: fixers 연결용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_id", nullable = false) // 자바에서는 객체로, DB에서는 slide_id FK로 매핑
    private Slide slide;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
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
