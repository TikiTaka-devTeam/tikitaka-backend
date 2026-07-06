package com.tikitaka.backend.slide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InsertSlideRequest(
        @Schema(description = "삽입할 수정 슬라이드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull
        UUID revisionSlideId,

        @Schema(description = "이 슬라이드 뒤에 삽입할 기존 슬라이드 ID", example = "123e4567-e89b-12d3-a456-426614174111")
        @NotNull
        UUID insertAfterSlideId
) {
}
