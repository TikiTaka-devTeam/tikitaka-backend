package com.tikitaka.backend.revision.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "수정용 임시 슬라이드 정보")
public record ReplacementSlideResponse(

        @Schema(description = "수정용 임시 슬라이드 ID", example = "123e4567-e89b-12d3-a456-426614174001")
        @JsonProperty("revision_slide_id")
        UUID revisionSlideId,

        @Schema(description = "수정 슬라이드 페이지 번호", example = "1")
        @JsonProperty("page_number")
        Integer pageNumber,

        @Schema(description = "슬라이드 이미지 URL", example = "https://example.com/new-slide-1.png")
        @JsonProperty("image_url")
        String imageUrl
) {
}
