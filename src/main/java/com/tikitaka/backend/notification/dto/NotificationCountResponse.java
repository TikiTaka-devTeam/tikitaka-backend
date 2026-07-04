package com.tikitaka.backend.notification.dto;

public record NotificationCountResponse(
        long totalCount,
        long readCount,
        long unreadCount
) {
}