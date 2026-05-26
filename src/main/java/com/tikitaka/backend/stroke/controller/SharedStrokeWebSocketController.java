package com.tikitaka.backend.stroke.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.layer.entity.SharedLayer;
import com.tikitaka.backend.layer.repository.SharedLayerRepository;
import com.tikitaka.backend.slide.entity.Slide;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.dto.SharedStrokeMessage;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import com.tikitaka.backend.stroke.repository.SharedStrokeRepository;
import com.tikitaka.backend.user.entity.Role;
import com.tikitaka.backend.user.entity.User;
import com.tikitaka.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SharedStrokeWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SlideRepository slideRepository;
    private final SharedLayerRepository sharedLayerRepository;
    private final SharedStrokeRepository sharedStrokeRepository;
    private final ObjectMapper objectMapper;

    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/shared-strokes")
    @Transactional
    public void handleSharedStroke(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload SharedStrokeMessage message,
            StompHeaderAccessor accessor
    ) {
        String token = extractToken(accessor);
        if (token == null) {
            log.warn("WS: Authorization 헤더 없음. spaceId={}, slideId={}", spaceId, slideId);
            return;
        }

        UUID userId;
        try {
            jwtProvider.isTokenValid(token);
            userId = UUID.fromString(jwtProvider.extractUserId(token));
        } catch (Exception e) {
            log.warn("WS: 유효하지 않은 토큰. error={}", e.getMessage());
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.PROFESSOR) {
            log.warn("WS: 교수가 아닌 사용자의 공유 필기 시도. userId={}", userId);
            return;
        }

        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));

        SharedLayer layer = sharedLayerRepository.findBySlideAndUser(slide, user)
                .orElseGet(() -> sharedLayerRepository.save(
                        SharedLayer.builder().slide(slide).user(user).build()
                ));

        SharedStrokeMessage.StrokeData strokeData = message.getStroke();
        SharedStroke savedStroke = sharedStrokeRepository.save(
                SharedStroke.builder()
                        .layer(layer)
                        .tool(strokeData.getTool())
                        .points(toJson(strokeData.getPoints()))
                        .content(strokeData.getContent())
                        .color(strokeData.getColor() != null ? strokeData.getColor() : "#000000")
                        .thickness(strokeData.getThickness() != null ? strokeData.getThickness() : 2.0f)
                        .strokeOrder(strokeData.getStrokeOrder() != null ? strokeData.getStrokeOrder() : 0)
                        .build()
        );

        SharedStrokeMessage broadcast = SharedStrokeMessage.builder()
                .type("SHARED_STROKE_CREATED")
                .strokeId(savedStroke.getId())
                .slideId(slideId)
                .stroke(strokeData)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/spaces/" + spaceId + "/slides/" + slideId + "/shared-strokes",
                broadcast
        );

        log.debug("WS: 공유 필기 브로드캐스트 완료. strokeId={}", savedStroke.getId());
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }
}
