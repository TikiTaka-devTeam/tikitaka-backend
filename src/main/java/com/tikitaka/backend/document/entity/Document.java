package com.tikitaka.backend.document.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.tikitaka.backend.space.entity.Space;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document {
 
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;
 
    @Column(name = "title", length = 255, nullable = false)
    private String title;
 
    @Column(name = "thumbnail_url", columnDefinition = "TEXT", nullable = false)
    private String thumbnailUrl;
 
    @Column(name = "pdf_url", columnDefinition = "TEXT", nullable = false)
    private String pdfUrl;
 
    @Column(name = "pdf_page_count", nullable = false)
    private Integer pdfPageCount;
 
    // PDF 수정 시마다 +1 → 슬라이드 버전 추적용
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;
 
    // 최초 업로드 PDF 보존 (NULL 가능)
    @Column(name = "original_pdf_url")
    private String originalPdfUrl;
  
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