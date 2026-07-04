package com.tikitaka.backend.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스페이스 공지 삭제 응답")
public record DeleteSpaceNoticeResponse(

        @Schema(description = "삭제 결과 메시지", example = "공지가 삭제되었습니다.")
        String message
) {
}