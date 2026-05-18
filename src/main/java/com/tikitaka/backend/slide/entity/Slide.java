package com.tikitaka.backend.slide.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.document.entity.Document;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "slides",
    uniqueConstraints = {
        // 동일 강의 내 동일 페이지+버전 중복 방지
        @UniqueConstraint(columnNames = {"document_id", "page_number", "version"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Slide {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
 
    @Column(name = "page_number", nullable = false)
    private Integer pageNumber; // CHECK (page_number > 0)
 
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;
 
    // 슬라이드 단일 페이지 이미지 URL
    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;
 
    // 다른 슬라이드로 교체되었는지 여부 (워터마크 표시 기준)
    @Column(name = "is_replaced", nullable = false)
    @Builder.Default
    private Boolean isReplaced = false;
 
    // 교체 전 원본 슬라이드 자기참조 (NULL 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_slide_id")
    private Slide originalSlide;
 
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
 
    @Column(name = "replaced_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime replacedAt;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime deletedAt;
}
