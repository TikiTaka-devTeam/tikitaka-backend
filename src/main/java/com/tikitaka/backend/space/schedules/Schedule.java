package com.tikitaka.backend.spaces.schedules;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.spaces.entity.Space;
 
@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Schedule {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;
 
    // DB에는 VARCHAR, Java에서 ENUM으로 관리
    // MONDAY ~ SUNDAY
    @Column(name = "day", length = 10, nullable = false)
    private String day;
 
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
 
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
 
    // ISO 8601 기준 타임존 (ex. "Asia/Seoul")
    @Column(name = "timezone", length = 30, nullable = false)
    private String timezone;
 
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
