package com.tikitaka.backend.question.cluster;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.document.entity.Document;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_clusters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionCluster {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // 클러스터는 document 단위로 생성 (강의자료별 Q&A 아카이빙)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
 
    // AI가 생성한 클러스터 카테고리 제목
    @Column(name = "summary_title", columnDefinition = "TEXT")
    private String summaryTitle;
 
    // 클러스터 내 질문 벡터들의 평균값
    // → 새 질문 유입 시 코사인 유사도로 클러스터 소속 판단
    @Column(name = "centroid", columnDefinition = "vector(768)")
    private float[] centroid;
 
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
