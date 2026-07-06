package com.tikitaka.backend.revision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "강의자료 수정용 PDF 임시 업로드 응답")
public record CreateDocumentReplacementResponse(

        @Schema(description = "수정 세션 ID", example = "123e4567-e89b-12d3-a456-426614174100")
        @JsonProperty("revision_id")
        UUID revisionId,

        @Schema(description = "기존 강의자료 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("original_document_id")
        UUID originalDocumentId,

        @Schema(description = "임시 업로드된 PDF URL", example = "https://example.com/replacement.pdf")
        @JsonProperty("replacement_pdf_url")
        String replacementPdfUrl,

        @Schema(description = "임시 슬라이드 목록")
        @JsonProperty("replacement_slides")
        List<ReplacementSlideResponse> replacementSlides
) {
}
