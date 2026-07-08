package com.tikitaka.backend.token.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.token.entity.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "기기 푸시 토큰 등록 요청")
public record DeviceTokenRegisterRequest(
    @Schema(description = "FCM 기기 토큰", example = "fcm-token-value")
    @JsonProperty("device_token")
    @NotBlank(message = "기기 토큰은 필수입니다")
    String deviceToken,

    @Schema(description = "기기 타입", example = "WEB")
    @JsonProperty("device_type")
    @NotNull(message = "기기 타입은 필수입니다")
    DeviceType deviceType
) {
}
