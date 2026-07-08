package com.tikitaka.backend.token.repository;

import com.tikitaka.backend.token.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    Optional<DeviceToken> findByUserIdAndDeviceToken(UUID userId, String deviceToken);

    Optional<DeviceToken> findByIdAndUserId(UUID id, UUID userId);

    List<DeviceToken> findByUserIdAndIsActiveTrue(UUID userId);
}
