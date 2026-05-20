package com.tikitaka.backend.space.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "space_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"space_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "강의 참여자 엔티티")
public class SpaceMember {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "강의 참여자 ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    @Schema(description = "참여 중인 강의")
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "참여 사용자")
    private User user;

    @Column(name = "validity", length = 10, nullable = false)
    @Builder.Default
    @Schema(description = "참여 승인 상태", example = "APPROVED")
    private String validity = "PENDING";

    @Column(name = "nickname", length = 100)
    @Schema(description = "사용자 기준 강의 별명", example = "운체")
    private String nickname;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    @Schema(description = "참여 요청 시각")
    private OffsetDateTime requestedAt;

    @Column(name = "approved_at", columnDefinition = "TIMESTAMPTZ")
    @Schema(description = "승인 시각")
    private OffsetDateTime approvedAt;

    @Column(name = "denied_at", columnDefinition = "TIMESTAMPTZ")
    @Schema(description = "거절 시각")
    private OffsetDateTime deniedAt;

    @Column(name = "last_accessed_at", columnDefinition = "TIMESTAMPTZ")
    @Schema(description = "최근 접속 시각")
    private OffsetDateTime lastAccessedAt;

    public void approve(OffsetDateTime approvedAt) {
        this.validity = "APPROVED";
        this.approvedAt = approvedAt;
        this.deniedAt = null;
    }

    public void deny(OffsetDateTime deniedAt) {
        this.validity = "DENIED";
        this.deniedAt = deniedAt;
        this.approvedAt = null;
    }
}
