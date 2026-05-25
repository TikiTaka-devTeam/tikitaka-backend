package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "강의자료 등록 응답")
public record CreateDocumentResponse(

        @Schema(description = "강의자료 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("document_id")
        UUID documentId,

        @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174111")
        @JsonProperty("space_id")
        UUID spaceId,

        @Schema(description = "강의자료 제목", example = "운영체제 1주차")
        String title,

        @Schema(description = "PDF URL", example = "/uploads/materials/uuid_운영체제_1주차.pdf")
        @JsonProperty("pdf_url")
        String pdfUrl,

        @Schema(description = "썸네일 URL", example = "/uploads/materials/default-thumbnail.png")
        @JsonProperty("thumbnail_url")
        String thumbnailUrl
) {
}