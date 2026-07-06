package com.tikitaka.backend.slide.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record InsertSlideResponse(
        @Schema(description = "원본 강의자료 ID")
        UUID documentId,
        @Schema(description = "삽입에 사용할 수정 슬라이드 ID")
        UUID revisionSlideId,
        @Schema(description = "이 슬라이드 뒤에 삽입됨")
        UUID insertAfterSlideId,
        @Schema(description = "수정 슬라이드 페이지 번호")
        Integer revisionPageNumber,
        @Schema(description = "삽입용 슬라이드 이미지 URL")
        String imageUrl
) {
}
