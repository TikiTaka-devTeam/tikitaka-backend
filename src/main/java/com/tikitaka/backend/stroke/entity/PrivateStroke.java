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

    @Column(name = "tool", length = 30, nullable = false)
    private String tool;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "points", columnDefinition = "jsonb", nullable = false)
    private String points;

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

    @Column(name = "stroke_order", nullable = false)
    @Builder.Default
    private Integer strokeOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.isDeleted = true;
    }
}