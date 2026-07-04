package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "스페이스 공지 수정 요청")
public record UpdateSpaceNoticeRequest(

        @Schema(description = "수정할 공지 제목", example = "중간고사 일정 변경")
        @Size(max = 255, message = "공지 제목은 255자를 넘을 수 없습니다.")
        String title,

        @Schema(description = "수정할 공지 내용", example = "중간고사 일정이 변경되었습니다.")
        String content,

        @Schema(description = "공지 고정 여부", example = "true")
        @JsonProperty("is_pinned")
        Boolean isPinned
) {
}