package com.tikitaka.backend.question.audio;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.question.answer.Answer;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "answer_audio_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnswerAudioFile {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // STT 처리는 비동기 파이프라인 → answers와 분리된 별도 테이블
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;
 
    // S3 또는 로컬 저장 경로
    @Column(name = "audio_url", columnDefinition = "TEXT", nullable = false)
    private String audioUrl;
 
    // PENDING | DONE | FAILED
    @Column(name = "stt_status", length = 10, nullable = false)
    @Builder.Default
    private String sttStatus = "PENDING";
 
    // STT 변환 결과 텍스트 (완료 전 NULL)
    @Column(name = "stt_text", columnDefinition = "TEXT")
    private String sttText;
 
    // STT 실패 시 예외 메시지
    @Column(name = "stt_error_message", columnDefinition = "TEXT")
    private String sttErrorMessage;
 
    @Column(name = "stt_completed_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime sttCompletedAt;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
