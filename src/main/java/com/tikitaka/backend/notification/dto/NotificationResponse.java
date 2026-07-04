package com.tikitaka.backend.notification.dto;

import com.tikitaka.backend.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        String type,
        Boolean isRead,
        String title,
        String message,
        UUID spaceId,
        String spaceName,
        UUID noticeId,
        UUID documentId,
        UUID answerId,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        UUID spaceId = null;
        String spaceName = null;
        UUID noticeId = null;
        UUID documentId = null;
        UUID answerId = null;

        String title = "";
        String message = "";

        if (notification.getNotice() != null) {
            noticeId = notification.getNotice().getId();
            spaceId = notification.getNotice().getSpace().getId();
            spaceName = notification.getNotice().getSpace().getName();

            title = notification.getNotice().getTitle();
            message = spaceName + "에 새 공지가 등록됐어요!";
        }

        if (notification.getDocument() != null) {
            documentId = notification.getDocument().getId();
            spaceId = notification.getDocument().getSpace().getId();
            spaceName = notification.getDocument().getSpace().getName();

            title = notification.getDocument().getTitle();

            if ("DOCUMENT_UPDATED".equals(notification.getType())) {
                message = spaceName + " 강의자료가 수정됐어요!";
            } else {
                message = spaceName + "에 새로운 강의자료가 업로드 됐어요!";
            }
        }

        if (notification.getAnswer() != null) {
            answerId = notification.getAnswer().getId();
            title = "질문에 답변이 달렸어요!";
            message = "남긴 질문에 새로운 답변이 등록됐어요.";
        }

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getIsRead(),
                title,
                message,
                spaceId,
                spaceName,
                noticeId,
                documentId,
                answerId,
                notification.getCreatedAt()
        );
    }
}