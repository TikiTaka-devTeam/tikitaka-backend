package com.tikitaka.backend.notification.service;

import com.tikitaka.backend.notification.dto.NotificationReadResponse;
import com.tikitaka.backend.notification.dto.NotificationResponse;
import com.tikitaka.backend.notification.dto.NotificationTargetResponse;
import com.tikitaka.backend.notification.entity.Notification;
import com.tikitaka.backend.notification.entity.NotificationType;
import com.tikitaka.backend.notification.repository.NotificationRepository;
import com.tikitaka.backend.space.entity.SpaceMember;
import com.tikitaka.backend.space.entity.SpaceNotice;
import com.tikitaka.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    public static final String TYPE_SPACE_NOTIFIED = "SPACE_NOTIFIED";

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId, Boolean isRead) {
        List<Notification> notifications = isRead == null
                ? notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);

        return notifications.stream()
                .map(notification -> NotificationResponse.from(
                        notification,
                        resolveMessage(notification),
                        resolveTargetUrl(notification)
                ))
                .toList();
    }

    public NotificationReadResponse markAsRead(UUID userId, UUID notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);
        notification.markAsRead();

        return new NotificationReadResponse(notification.getId(), notification.getIsRead());
    }

    @Transactional(readOnly = true)
    public NotificationTargetResponse getNotificationTarget(UUID userId, UUID notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);

        return new NotificationTargetResponse(
                notification.getId(),
                notification.getType(),
                resolveTargetType(notification),
                resolveTargetUrl(notification)
        );
    }

    public void createSpaceNoticeNotifications(SpaceNotice notice, List<User> students) {
        if (students == null || students.isEmpty()) {
            return;
        }

        List<Notification> notifications = students.stream()
                .map(student -> Notification.builder()
                        .user(student)
                        .notice(notice)
                        .type(TYPE_SPACE_NOTIFIED)
                        .isRead(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public void createInvitationAcceptedNotification(SpaceMember member) {
        Notification notification = Notification.builder()
                .user(member.getUser())
                .space(member.getSpace())
                .type(NotificationType.INVITATION_ACCEPTED.name())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public void deleteByNoticeId(SpaceNotice notice) {
        notificationRepository.deleteByNoticeId(notice.getId());
    }

    private Notification getOwnedNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "알림에 접근할 권한이 없습니다.");
        }

        return notification;
    }

    private String resolveMessage(Notification notification) {
        NotificationType type = parseType(notification.getType());

        return switch (type) {
            case SPACE_NOTIFIED -> "새 공지가 등록되었습니다.";
            case DOCUMENT_UPLOADED -> "새 강의자료가 등록되었습니다.";
            case DOCUMENT_UPDATED -> "강의자료가 수정되었습니다.";
            case ANSWER_POSTED -> "내 질문에 답변이 등록되었습니다.";
            case INVITATION_ACCEPTED -> "강의 참여 요청이 승인되었습니다.";
        };
    }

    private String resolveTargetType(Notification notification) {
        NotificationType type = parseType(notification.getType());

        return switch (type) {
            case SPACE_NOTIFIED -> "SPACE_NOTICE";
            case DOCUMENT_UPLOADED, DOCUMENT_UPDATED -> "DOCUMENT";
            case ANSWER_POSTED -> "QUESTION";
            case INVITATION_ACCEPTED -> "SPACE";
        };
    }

    private String resolveTargetUrl(Notification notification) {
        NotificationType type = parseType(notification.getType());

        return switch (type) {
            case SPACE_NOTIFIED -> {
                if (notification.getNotice() == null) {
                    yield null;
                }
                yield "/spaces/" + notification.getNotice().getSpace().getId()
                        + "/notices/" + notification.getNotice().getId();
            }
            case DOCUMENT_UPLOADED, DOCUMENT_UPDATED -> notification.getDocument() == null
                    ? null
                    : "/documents/" + notification.getDocument().getId();
            case ANSWER_POSTED -> {
                if (notification.getAnswer() == null) {
                    yield null;
                }
                yield "/questions/" + notification.getAnswer().getQuestion().getId();
            }
            case INVITATION_ACCEPTED -> notification.getSpace() == null
                    ? null
                    : "/spaces/" + notification.getSpace().getId();
        };
    }

    private NotificationType parseType(String type) {
        try {
            return NotificationType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 알림 타입입니다.");
        }
    }
}
