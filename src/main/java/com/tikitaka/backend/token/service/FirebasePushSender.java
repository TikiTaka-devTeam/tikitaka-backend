package com.tikitaka.backend.token.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.tikitaka.backend.token.entity.DeviceToken;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
public class FirebasePushSender implements PushSender {

    private final String serviceAccountJson;
    private final String serviceAccountPath;
    private FirebaseMessaging firebaseMessaging;

    public FirebasePushSender(
        @Value("${app.firebase.service-account-json:}") String serviceAccountJson,
        @Value("${app.firebase.service-account-path:}") String serviceAccountPath
    ) {
        this.serviceAccountJson = serviceAccountJson;
        this.serviceAccountPath = serviceAccountPath;
    }

    @PostConstruct
    public void initialize() {
        if (!StringUtils.hasText(serviceAccountJson) && !StringUtils.hasText(serviceAccountPath)) {
            log.warn("Firebase service account is not configured. Push sending is disabled.");
            return;
        }

        try (InputStream credentialsStream = openCredentialsStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(options)
                    : FirebaseApp.getInstance();

            this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
        } catch (IOException e) {
            throw new IllegalStateException("Firebase 서비스 계정 설정을 읽을 수 없습니다.", e);
        }
    }

    @Override
    public void send(List<DeviceToken> deviceTokens, String title, String body) {
        if (firebaseMessaging == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Firebase 푸시 설정이 필요합니다.");
        }

        int successCount = 0;
        for (DeviceToken deviceToken : deviceTokens) {
            Message message = Message.builder()
                    .setToken(deviceToken.getDeviceToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            try {
                firebaseMessaging.send(message);
                successCount++;
            } catch (FirebaseMessagingException e) {
                log.warn("Failed to send push. deviceTokenId={}, firebaseErrorCode={}",
                        deviceToken.getId(), e.getErrorCode(), e);
            }
        }

        if (successCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "푸시 발송에 실패했습니다.");
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (StringUtils.hasText(serviceAccountPath)) {
            return new FileInputStream(serviceAccountPath);
        }

        return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
    }
}
