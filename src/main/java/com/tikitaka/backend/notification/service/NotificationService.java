package com.tikitaka.backend.notification.service;

import com.tikitaka.backend.notification.dto.NotificationCountResponse;
import com.tikitaka.backend.notification.dto.NotificationReadResponse;
import com.tikitaka.backend.notification.dto.NotificationResponse;
import com.tikitaka.backend.notification.entity.Notification;
import com.tikitaka.backend.notification.repository.NotificationRepository;
import com.tikitaka.backend.space.entity.SpaceNotice;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId, Boolean isRead) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        List<Notification> notifications;

        if (isRead == null) {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        }

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationCountResponse getMyNotificationCounts(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        long totalCount = notificationRepository.countByUserId(userId);
        long unreadCount = notificationRepository.countByUserIdAndIsRead(userId, false);
        long readCount = notificationRepository.countByUserIdAndIsRead(userId, true);

        return new NotificationCountResponse(totalCount, readCount, unreadCount);
    }

    public NotificationReadResponse readNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

        notification.markAsRead();

        return new NotificationReadResponse(notification.getId(), notification.getIsRead());
    }
}