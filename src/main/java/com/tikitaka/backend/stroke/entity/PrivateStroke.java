package com.tikitaka.backend.stroke.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import com.tikitaka.backend.layer.entity.PrivateLayer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
 
@Entity
@Table(name = "private_strokes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PrivateStroke {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private PrivateLayer layer;
 
    // DB에는 VARCHAR, Java에서 ENUM으로 관리
    // PEN | HIGHLIGHTER | ERASER | Q_POINT | Q_LIST | KEYBOARD | FIXER | ETC
    // Q_POINT 사용 시 → questions 테이블에도 동시 INSERT (트랜잭션 묶음)
    @Column(name = "tool", length = 30, nullable = false)
    private String tool;
 
    // [{x:10, y:10}, {x:11, y:11}, ...] 형태의 좌표 배열
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "points", columnDefinition = "jsonb", nullable = false)
    private String points;
 
    // tool = KEYBOARD 일 때만 사용, 나머지는 NULL
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
 
    @Column(name = "color", length = 7, nullable = false)
    @Builder.Default
    private String color = "#000000";
 
    @Column(name = "thickness", nullable = false)
    @Builder.Default
    private Float thickness = 2.0f;
 
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
 
    // 스트로크 렌더링 순서 (z-order)
    @Column(name = "stroke_order", nullable = false)
    @Builder.Default
    private Integer strokeOrder = 0;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}