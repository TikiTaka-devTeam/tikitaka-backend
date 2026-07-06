package com.tikitaka.backend.slide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ReplaceSlideResponse(
        @Schema(description = "기존 슬라이드 ID")
        UUID slideId,
        @Schema(description = "연결된 수정 슬라이드 ID")
        UUID revisionSlideId,
        @Schema(description = "원본 강의자료 ID")
        UUID documentId,
        @Schema(description = "기존 슬라이드 페이지 번호")
        Integer pageNumber,
        @Schema(description = "교체에 사용할 수정 슬라이드 이미지 URL")
        String replacementImageUrl
) {
}
