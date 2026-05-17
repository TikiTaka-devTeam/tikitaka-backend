package com.tikitaka.backend.question.answer;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.question.entity.Question;
import com.tikitaka.backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(
    name = "answers",
    uniqueConstraints = {
        // 하나의 질문에 하나의 답변만 (1:1)
        @UniqueConstraint(columnNames = {"question_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Answer {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private Question question;
 
    // AI 답변이면 NULL, 교수 답변이면 교수 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private User professor;
 
    // PROFESSOR | AI → answerer_type으로 분기
    @Column(name = "answerer_type", length = 20, nullable = false)
    private String answererType;
 
    // 텍스트 답변 or STT 변환 결과 포함
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
 
    // AI 답변 시 어떤 모델 사용했는지 ex) "gpt-4o" (NULL 가능)
    @Column(name = "ai_model", length = 100)
    private String aiModel;
 
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
