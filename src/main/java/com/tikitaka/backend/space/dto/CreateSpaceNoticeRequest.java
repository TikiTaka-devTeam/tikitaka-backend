package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "스페이스 공지 생성 요청")
public record CreateSpaceNoticeRequest(

        @Schema(description = "공지 제목", example = "중간고사 안내")
        @NotBlank(message = "공지 제목은 필수입니다.")
        @Size(max = 255, message = "공지 제목은 255자를 넘을 수 없습니다.")
        String title,

        @Schema(description = "공지 내용", example = "다음 주 중간고사를 진행합니다.")
        @NotBlank(message = "공지 내용은 필수입니다.")
        String content,

        @Schema(description = "공지 고정 여부", example = "false")
        @JsonProperty("is_pinned")
        Boolean isPinned
) {
}