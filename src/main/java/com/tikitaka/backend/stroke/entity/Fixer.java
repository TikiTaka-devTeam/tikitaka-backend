package com.tikitaka.backend.stroke.entity;

import com.tikitaka.backend.layer.entity.PrivateLayer;
import com.tikitaka.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

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

    // 교수 개인 레이어
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private PrivateLayer layer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    // 0 ~ 1 비율 좌표
    @Column(name = "x_ratio", nullable = false)
    private Double xRatio;

    // 0 ~ 1 비율 좌표
    @Column(name = "y_ratio", nullable = false)
    private Double yRatio;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private Boolean isChecked = false;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMPTZ", updatable = false)
    private OffsetDateTime createdAt;

    public void check() {
        this.isChecked = true;
    }
}