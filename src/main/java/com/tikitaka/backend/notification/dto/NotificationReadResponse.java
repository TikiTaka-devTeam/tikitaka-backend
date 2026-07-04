package com.tikitaka.backend.notification.dto;

import java.util.UUID;

public record NotificationReadResponse(
        UUID notificationId,
        Boolean isRead
) {
}