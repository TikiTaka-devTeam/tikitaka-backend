package com.tikitaka.backend.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.token.entity.DeviceToken;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "기기 푸시 토큰 응답")
public record DeviceTokenResponse(
    @Schema(description = "기기 토큰 ID")
    @JsonProperty("device_token_id")
    UUID deviceTokenId,

    @Schema(description = "활성 여부", example = "true")
    @JsonProperty("is_active")
    Boolean isActive
) {
    public static DeviceTokenResponse from(DeviceToken deviceToken) {
        return new DeviceTokenResponse(deviceToken.getId(), deviceToken.getIsActive());
    }
}
