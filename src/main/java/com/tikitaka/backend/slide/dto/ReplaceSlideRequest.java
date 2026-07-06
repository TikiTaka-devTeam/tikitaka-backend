package com.tikitaka.backend.slide.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReplaceSlideRequest(
        @Schema(description = "교체에 사용할 수정 슬라이드 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull
        UUID revisionSlideId
) {
}
