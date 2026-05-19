package com.tikitaka.backend.space.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "space_members",
    uniqueConstraints = {
        // 한 space에 같은 user가 중복 참여 불가
        @UniqueConstraint(columnNames = {"space_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SpaceMember {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    // APPROVED / DENIED / PENDING
    @Column(name = "validity", length = 10, nullable = false)
    @Builder.Default
    private String validity = "PENDING";

    // 강의별 닉네임
    @Column(name = "nickname", length = 100)
    private String nickname;
 
    // 학생이 참여 요청한 시간
    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private OffsetDateTime requestedAt;
 
    // 교수가 승인한 시간 (NULL 가능)
    @Column(name = "approved_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime approvedAt;
 
    // 교수가 거절한 시간 (NULL 가능)
    @Column(name = "denied_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime deniedAt;
 
    // 최근 접속 시간 → recent space 정렬 기준
    @Column(name = "last_accessed_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime lastAccessedAt;

}
