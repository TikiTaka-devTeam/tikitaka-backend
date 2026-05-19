package com.tikitaka.backend.space.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "강의 엔티티")
public class Space {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "강의 ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    @Schema(description = "강의를 생성한 교수")
    private User professor;

    @Column(name = "name", length = 255, nullable = false)
    @Schema(description = "강의명", example = "운영체제")
    private String name;

    @Column(name = "semester", length = 20, nullable = false)
    @Schema(description = "학기", example = "2026-1")
    private String semester;

    @Column(name = "space_code", length = 8, unique = true, nullable = false)
    @Schema(description = "학생 참여용 초대 코드", example = "A1B2C3D4")
    private String spaceCode;

    @Column(name = "color", length = 20)
    @Schema(description = "강의 대표 색상", example = "#4F46E5")
    private String color;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "수정 시각")
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
