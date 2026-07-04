package com.tikitaka.backend.notification.repository;

import com.tikitaka.backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    void deleteByNoticeId(UUID noticeId);
}