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
    name = "shared_layers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"slide_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SharedLayer {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // 교수 필기 → 전체 학생 브로드캐스트 대상 슬라이드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_id", nullable = false)
    private Slide slide;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    // FALSE → 학생 화면에 공유 필기 숨김
    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;
 
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
