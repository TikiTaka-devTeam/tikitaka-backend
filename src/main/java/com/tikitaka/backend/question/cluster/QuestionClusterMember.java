package com.tikitaka.backend.question.cluster;

import jakarta.persistence.*;
import lombok.*;
 
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.question.entity.Question;
 
@Entity
@Table(
    name = "question_cluster_members",
    uniqueConstraints = {
        // 같은 질문이 같은 클러스터에 중복 등록 방지
        @UniqueConstraint(columnNames = {"cluster_id", "question_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuestionClusterMember {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private QuestionCluster cluster;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
 
    // centroid와의 코사인 유사도 (0~1)
    @Column(name = "similarity", nullable = false)
    @Builder.Default
    private Float similarity = 0.0f;
 
    // 클러스터 대표 질문 여부 (요약 시 사용)
    @Column(name = "is_representative", nullable = false)
    @Builder.Default
    private Boolean isRepresentative = false;
}
