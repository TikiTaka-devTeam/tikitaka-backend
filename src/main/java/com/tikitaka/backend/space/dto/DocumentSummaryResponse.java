package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "강의자료 목록 조회 응답")
public record DocumentSummaryResponse(

        @Schema(description = "강의자료 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("document_id")
        UUID documentId,

        @Schema(description = "강의자료 제목", example = "운영체제 1주차")
        String title,

        @Schema(description = "썸네일 URL", example = "https://example.com/thumb.png")
        @JsonProperty("thumbnail_url")
        String thumbnailUrl,

        @Schema(description = "PDF URL", example = "https://example.com/file.pdf")
        @JsonProperty("pdf_url")
        String pdfUrl,

        @Schema(description = "업로드 날짜", example = "2026-05-04")
        @JsonProperty("uploaded_at")
        LocalDate uploadedAt
) {
}