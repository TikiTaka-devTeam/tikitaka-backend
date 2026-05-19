package com.tikitaka.backend.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.document.entity.Document;
import com.tikitaka.backend.question.answer.Answer;
import com.tikitaka.backend.space.entity.SpaceNotice;
import com.tikitaka.backend.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;
 
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false    )
    private UUID id;
 
    // 알림 수신자 (학생)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    // type에 따라 연결 대상이 다름 → 각각 NULL 가능
    // DOCUMENT_UPLOADED | DOCUMENT_UPDATED → document_id 채움
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;
 
    // ANSWER_POSTED → answer_id 채움
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;
 
    // SPACE_NOTIFIED → notice_id 채움
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private SpaceNotice notice;
 
    // Java에서 ENUM 처리:
    // DOCUMENT_UPLOADED | DOCUMENT_UPDATED | ANSWER_POSTED | SPACE_NOTIFIED
    @Column(name = "type", length = 50, nullable = false)
    private String type;
 
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
    }
}
