package com.tikitaka.backend.stroke.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.global.jwt.JwtProvider;
import com.tikitaka.backend.slide.repository.SlideRepository;
import com.tikitaka.backend.stroke.dto.SharedStrokeMessage;
import com.tikitaka.backend.stroke.dto.StrokeAckMessage;
import com.tikitaka.backend.stroke.dto.StrokePoint;
import com.tikitaka.backend.stroke.dto.StrokeResyncRequest;
import com.tikitaka.backend.stroke.entity.SharedStroke;
import com.tikitaka.backend.stroke.repository.SharedStrokeRepository;
import com.tikitaka.backend.stroke.service.StrokeRedisService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SharedStrokeWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SlideRepository slideRepository;
    private final SharedStrokeRepository sharedStrokeRepository;
    private final StrokeRedisService strokeRedisService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<UUID, AtomicInteger> slideSeqCounters =
            new ConcurrentHashMap<>();

    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/shared-strokes")
    @Transactional(readOnly = true)
    public void handleSharedStroke(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload SharedStrokeMessage message,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);
        if (user == null) return;

        if (user.getRole() != Role.PROFESSOR) {
            log.warn("WS: 교수가 아닌 사용자의 공유 필기 시도. userId={}", user.getId());
            return;
        }

        slideRepository.findById(slideId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLIDE_NOT_FOUND));

        String type = message.getType();

        if ("SHARED_STROKE_DELETE".equals(type) || "SHARED_STROKE_DELETED".equals(type)) {
            handleSharedStrokeDelete(spaceId, slideId, message);
            return;
        }

        handleSharedStrokeCreate(spaceId, slideId, message);
    }

    // 교수 필기 생성 브로드캐스트
    private void handleSharedStrokeCreate(
            UUID spaceId,
            UUID slideId,
            SharedStrokeMessage message
    ) {
        SharedStrokeMessage.StrokeData strokeData = message.getStroke();

        if (strokeData == null || strokeData.getPoints() == null || strokeData.getPoints().isEmpty()) {
            log.warn("WS: 비어있는 공유 필기 수신. slideId={}", slideId);
            return;
        }

        int strokeSeq = nextLiveStrokeSeq(slideId);

        UUID liveStrokeId = message.getStrokeId() != null
                ? message.getStrokeId()
                : UUID.randomUUID();

        SharedStrokeMessage.StrokeData broadcastStrokeData =
                SharedStrokeMessage.StrokeData.builder()
                        .tool(strokeData.getTool())
                        .points(strokeData.getPoints())
                        .color(strokeData.getColor() != null ? strokeData.getColor() : "#000000")
                        .thickness(strokeData.getThickness() != null ? strokeData.getThickness() : 2.0f)
                        .content(strokeData.getContent())
                        .strokeOrder(strokeData.getStrokeOrder() != null ? strokeData.getStrokeOrder() : 0)
                        .build();

        SharedStrokeMessage broadcast = SharedStrokeMessage.builder()
                .type("SHARED_STROKE_CREATED")
                .strokeId(liveStrokeId)
                .slideId(slideId)
                .strokeSeq(strokeSeq)
                .stroke(broadcastStrokeData)
                .build();

        strokeRedisService.saveStroke(slideId, strokeSeq, broadcast);

        messagingTemplate.convertAndSend(
                strokesTopic(spaceId, slideId),
                broadcast
        );

        log.debug(
                "WS: 공유 필기 생성 브로드캐스트. strokeId={}, strokeSeq={}, points={}",
                liveStrokeId,
                strokeSeq,
                strokeData.getPoints().size()
        );
    }

    // 교수 필기 삭제 브로드캐스트
    private void handleSharedStrokeDelete(
            UUID spaceId,
            UUID slideId,
            SharedStrokeMessage message
    ) {
        if (message.getStrokeId() == null) {
            log.warn("WS: 삭제할 strokeId 없음. slideId={}", slideId);
            return;
        }

        int strokeSeq = nextLiveStrokeSeq(slideId);

        SharedStrokeMessage broadcast = SharedStrokeMessage.builder()
                .type("SHARED_STROKE_DELETED")
                .strokeId(message.getStrokeId())
                .slideId(slideId)
                .strokeSeq(strokeSeq)
                .stroke(
                        SharedStrokeMessage.StrokeData.builder()
                                .points(Collections.emptyList())
                                .build()
                )
                .build();

        strokeRedisService.saveStroke(slideId, strokeSeq, broadcast);

        messagingTemplate.convertAndSend(
                strokesTopic(spaceId, slideId),
                broadcast
        );

        log.debug(
                "WS: 공유 필기 삭제 브로드캐스트. strokeId={}, strokeSeq={}",
                message.getStrokeId(),
                strokeSeq
        );
    }

    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/ack")
    public void handleAck(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload StrokeAckMessage ackMessage,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);
        if (user == null || ackMessage.getLastReceivedStrokeSeq() == null) return;

        strokeRedisService.saveAck(
                user.getId(),
                slideId,
                ackMessage.getLastReceivedStrokeSeq()
        );

        log.debug(
                "WS: ACK 수신. userId={}, slideId={}, lastSeq={}",
                user.getId(),
                slideId,
                ackMessage.getLastReceivedStrokeSeq()
        );
    }

    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/resync")
    @Transactional(readOnly = true)
    public void handleResync(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload StrokeResyncRequest request,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);
        if (user == null || request.getLastReceivedStrokeSeq() == null) return;

        int lastSeq = request.getLastReceivedStrokeSeq();

        log.debug(
                "WS: 재동기화 요청. userId={}, slideId={}, lastSeq={}",
                user.getId(),
                slideId,
                lastSeq
        );

        List<SharedStrokeMessage> missing =
                strokeRedisService.getStrokesAfter(slideId, lastSeq);

        if (missing.isEmpty()) {
            missing = sharedStrokeRepository.findMissingStrokesBySlideId(slideId, lastSeq)
                    .stream()
                    .map(stroke -> toResyncMessage(slideId, stroke))
                    .toList();
        }

        if (missing.isEmpty()) {
            log.debug("WS: 재동기화 대상 없음. slideId={}, lastSeq={}", slideId, lastSeq);
            return;
        }

        for (SharedStrokeMessage stroke : missing) {
            SharedStrokeMessage resyncMsg = SharedStrokeMessage.builder()
                    .type(stroke.getType() != null ? stroke.getType() : "SHARED_STROKE_RESYNC")
                    .strokeId(stroke.getStrokeId())
                    .slideId(slideId)
                    .strokeSeq(stroke.getStrokeSeq())
                    .stroke(stroke.getStroke())
                    .build();

            messagingTemplate.convertAndSend(
                    strokesTopic(spaceId, slideId),
                    resyncMsg
            );
        }

        log.debug(
                "WS: 재동기화 완료. slideId={}, 재전송 개수={}",
                slideId,
                missing.size()
        );
    }

    private int nextLiveStrokeSeq(UUID slideId) {
        return slideSeqCounters
                .computeIfAbsent(slideId, id -> new AtomicInteger(0))
                .incrementAndGet();
    }

    private SharedStrokeMessage toResyncMessage(UUID slideId, SharedStroke stroke) {
        return SharedStrokeMessage.builder()
                .type("SHARED_STROKE_RESYNC")
                .strokeId(stroke.getId())
                .slideId(slideId)
                .strokeSeq(stroke.getStrokeSeq())
                .stroke(SharedStrokeMessage.StrokeData.builder()
                        .tool(stroke.getTool())
                        .points(parsePoints(stroke.getPoints()))
                        .color(stroke.getColor())
                        .thickness(stroke.getThickness())
                        .content(stroke.getContent())
                        .strokeOrder(stroke.getStrokeOrder())
                        .build())
                .build();
    }

    private User resolveUser(StompHeaderAccessor accessor, UUID spaceId, UUID slideId) {
        String token = extractToken(accessor);

        if (token == null) {
            log.warn("WS: Authorization 헤더 없음. spaceId={}, slideId={}", spaceId, slideId);
            return null;
        }

        try {
            jwtProvider.isTokenValid(token);

            UUID userId = UUID.fromString(jwtProvider.extractUserId(token));

            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        } catch (Exception e) {
            log.warn("WS: 유효하지 않은 토큰. error={}", e.getMessage());
            return null;
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private String strokesTopic(UUID spaceId, UUID slideId) {
        return "/topic/spaces/" + spaceId + "/slides/" + slideId + "/shared-strokes";
    }

    private List<StrokePoint> parsePoints(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<StrokePoint>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}