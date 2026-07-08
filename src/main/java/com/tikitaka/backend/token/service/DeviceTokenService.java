package com.tikitaka.backend.token.service;

import com.tikitaka.backend.token.dto.DeviceTokenRegisterRequest;
import com.tikitaka.backend.token.dto.DeviceTokenResponse;
import com.tikitaka.backend.token.dto.PushTestRequest;
import com.tikitaka.backend.token.dto.PushTestResponse;
import com.tikitaka.backend.token.entity.DeviceToken;
import com.tikitaka.backend.token.repository.DeviceTokenRepository;
import com.tikitaka.backend.user.entity.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushSender pushSender;

    public DeviceTokenResponse register(User user, DeviceTokenRegisterRequest request) {
        DeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndDeviceToken(user.getId(), request.deviceToken())
                .map(existing -> {
                    existing.reactivate(request.deviceType());
                    return existing;
                })
                .orElseGet(() -> deviceTokenRepository.save(DeviceToken.builder()
                        .user(user)
                        .deviceToken(request.deviceToken())
                        .deviceType(request.deviceType())
                        .isActive(true)
                        .build()));

        return DeviceTokenResponse.from(deviceToken);
    }

    public DeviceTokenResponse deactivate(UUID userId, UUID deviceTokenId) {
        DeviceToken deviceToken = deviceTokenRepository.findByIdAndUserId(deviceTokenId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기기 토큰을 찾을 수 없습니다."));

        deviceToken.deactivate();

        return DeviceTokenResponse.from(deviceToken);
    }

    @Transactional(readOnly = true)
    public PushTestResponse sendTestPush(UUID userId, PushTestRequest request) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(userId);
        if (activeTokens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "활성화된 기기 토큰을 찾을 수 없습니다.");
        }

        pushSender.send(activeTokens, request.title(), request.body());

        return PushTestResponse.sent();
    }
}
