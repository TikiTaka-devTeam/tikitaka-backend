package com.tikitaka.backend.notification.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 이동 대상 응답")
public record NotificationTargetResponse(
    @Schema(description = "알림 ID")
    @JsonProperty("notification_id")
    UUID notificationId,

    @Schema(description = "알림 타입", example = "DOCUMENT_UPDATED")
    String type,

    @Schema(description = "이동 대상 타입", example = "DOCUMENT")
    @JsonProperty("target_type")
    String targetType,

    @Schema(description = "이동 대상 URL", example = "/documents/{document_id}")
    @JsonProperty("target_url")
    String targetUrl
) {
}
