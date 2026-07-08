package com.tikitaka.backend.notification.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 읽음 처리 응답")
public record NotificationReadResponse(
    @Schema(description = "알림 ID")
    @JsonProperty("notification_id")
    UUID notificationId,

    @Schema(description = "읽음 여부", example = "true")
    @JsonProperty("is_read")
    Boolean isRead
) {
}
