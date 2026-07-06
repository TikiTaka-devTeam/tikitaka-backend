package com.tikitaka.backend.revision.entity;

import com.tikitaka.backend.slide.entity.Slide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "revision_slides",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"document_revision_id", "page_number"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RevisionSlide {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_revision_id", nullable = false)
    private DocumentRevision documentRevision;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_slide_id")
    private Slide targetSlide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insert_after_slide_id")
    private Slide insertAfterSlide;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void assignTargetSlide(Slide targetSlide) {
        this.targetSlide = targetSlide;
        this.insertAfterSlide = null;
    }

    public void assignInsertAfterSlide(Slide insertAfterSlide) {
        this.insertAfterSlide = insertAfterSlide;
        this.targetSlide = null;
    }
}
