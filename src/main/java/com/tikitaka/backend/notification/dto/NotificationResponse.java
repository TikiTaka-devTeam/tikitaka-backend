package com.tikitaka.backend.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.notification.entity.Notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 목록 응답")
public record NotificationResponse(
    @Schema(description = "알림 ID")
    @JsonProperty("notification_id")
    UUID notificationId,

    @Schema(description = "알림 타입", example = "SPACE_NOTIFIED")
    String type,

    @Schema(description = "알림 메시지", example = "새 공지가 등록되었습니다.")
    String message,

    @Schema(description = "이동 대상 URL", example = "/spaces/{space_id}/notices/{notice_id}")
    @JsonProperty("target_url")
    String targetUrl,

    @Schema(description = "읽음 여부", example = "false")
    @JsonProperty("is_read")
    Boolean isRead,

    @Schema(description = "생성 시각")
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification, String message, String targetUrl) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            message,
            targetUrl,
            notification.getIsRead(),
            notification.getCreatedAt()
        );
    }
}
