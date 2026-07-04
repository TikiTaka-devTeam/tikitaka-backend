package com.tikitaka.backend.notification.repository;

import com.tikitaka.backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, Boolean isRead);

    long countByUserId(UUID userId);

    long countByUserIdAndIsRead(UUID userId, Boolean isRead);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}