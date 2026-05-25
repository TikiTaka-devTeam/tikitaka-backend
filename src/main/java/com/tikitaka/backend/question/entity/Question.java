package com.tikitaka.backend.question.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import com.tikitaka.backend.question.enums.QuestionStatus;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.stroke.entity.PrivateStroke;
import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_id", nullable = false)
    private Slide slide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Q_POINT stroke 생성과 동시에 INSERT → 서비스 레이어에서 트랜잭션 묶음 필수
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_stroke_id", nullable = false, unique = true)
    private PrivateStroke privateStroke;

    // TRUE → 학생 이름 숨김
    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = true;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    // Canvas 좌표 비율 → 슬라이드 위치 기반 질문 핀 표시용
    @Column(name = "x_ratio")
    private Float xRatio;

    @Column(name = "y_ratio")
    private Float yRatio;

    // PENDING | ANSWERED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private QuestionStatus status = QuestionStatus.PENDING;

    // pgvector 확장 사용 → Sentence-BERT 768차원 임베딩
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    // AI 정제 완료 여부
    @Column(name = "is_refined", nullable = false)
    @Builder.Default
    private Boolean isRefined = false;

    // AI가 정제한 질문 내용 (NULL 가능)
    @Column(name = "refined_content", columnDefinition = "TEXT")
    private String refinedContent;

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

    public void markAsAnswered() {
        this.status = QuestionStatus.ANSWERED;
    }
}