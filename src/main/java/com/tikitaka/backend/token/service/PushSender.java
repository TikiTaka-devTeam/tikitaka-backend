package com.tikitaka.backend.token.service;

import com.tikitaka.backend.token.entity.DeviceToken;
import java.util.List;

public interface PushSender {

    void send(List<DeviceToken> deviceTokens, String title, String body);
}
