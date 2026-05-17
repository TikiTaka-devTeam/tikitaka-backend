package com.tikitaka.backend.stroke.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.layer.entity.PrivateLayer;
import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "fixers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Fixer {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // 교수의 private_layer에만 연결 (서비스 레이어에서 role 체크 필요)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private PrivateLayer layer;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;
 
    // Canvas 좌표 비율 (0~1) → 슬라이드 크기 변해도 위치 유지
    @Column(name = "x_ratio", nullable = false)
    private Float xRatio;
 
    @Column(name = "y_ratio", nullable = false)
    private Float yRatio;
 
    // tool = KEYBOARD일 때만 사용
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
 
    // TRUE로 바뀌면 삭제 처리 (소프트 딜리트 대신 체크 방식)
    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private Boolean isChecked = false;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}