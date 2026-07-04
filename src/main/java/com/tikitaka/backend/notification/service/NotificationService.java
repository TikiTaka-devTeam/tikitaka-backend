package com.tikitaka.backend.notification.service;

import com.tikitaka.backend.notification.entity.Notification;
import com.tikitaka.backend.notification.repository.NotificationRepository;
import com.tikitaka.backend.space.entity.SpaceNotice;
import com.tikitaka.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    public static final String TYPE_SPACE_NOTIFIED = "SPACE_NOTIFIED";

    private final NotificationRepository notificationRepository;

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

    public void deleteByNoticeId(SpaceNotice notice) {
        notificationRepository.deleteByNoticeId(notice.getId());
    }
}