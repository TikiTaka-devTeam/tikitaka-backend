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
@Table(name = "space_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "강의 공지 엔티티")
public class SpaceNotice {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "공지 ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    @Schema(description = "공지 대상 강의")
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    @Schema(description = "공지를 작성한 교수")
    private User professor;

    @Column(name = "title", length = 255, nullable = false)
    @Schema(description = "공지 제목", example = "중간고사 안내")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "공지 내용", example = "다음 주 중간고사를 진행합니다.")
    private String content;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean isPinned = false;

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
