package com.tikitaka.backend.space.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "강의 시간표 엔티티")
public class Schedule {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "시간표 ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    @Schema(description = "연결된 강의")
    private Space space;

    @Column(name = "day", length = 10, nullable = false)
    @Schema(description = "요일", example = "MONDAY")
    private String day;

    @Column(name = "start_time", nullable = false)
    @Schema(description = "강의 시작 시간", example = "09:00:00")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @Schema(description = "강의 종료 시간", example = "10:30:00")
    private LocalTime endTime;

    @Column(name = "timezone", length = 30, nullable = false)
    @Schema(description = "시간대", example = "Asia/Seoul")
    private String timezone;

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
