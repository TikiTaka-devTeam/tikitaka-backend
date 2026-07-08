package com.tikitaka.backend.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 테스트 발송 응답")
public record PushTestResponse(
    @Schema(description = "푸시 발송 상태", example = "SENT")
    @JsonProperty("push_status")
    String pushStatus
) {
    public static PushTestResponse sent() {
        return new PushTestResponse("SENT");
    }
}
