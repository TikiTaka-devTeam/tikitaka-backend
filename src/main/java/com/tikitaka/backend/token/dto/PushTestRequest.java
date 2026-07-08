package com.tikitaka.backend.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "푸시 테스트 발송 요청")
public record PushTestRequest(
    @Schema(description = "푸시 제목", example = "테스트 알림")
    @NotBlank(message = "제목은 필수입니다")
    String title,

    @Schema(description = "푸시 본문", example = "푸시 알림 테스트입니다.")
    @NotBlank(message = "본문은 필수입니다")
    String body
) {
}
