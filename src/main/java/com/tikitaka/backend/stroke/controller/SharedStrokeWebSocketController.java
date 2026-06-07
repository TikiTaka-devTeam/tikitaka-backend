package com.tikitaka.backend.stroke.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.global.exception.CustomException;
import com.tikitaka.backend.global.exception.ErrorCode;
import com.tikitaka.backend.global.jwt.JwtProvider;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class SharedStrokeWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SharedStrokeRepository sharedStrokeRepository;
    private final StrokeRedisService strokeRedisService;
    private final ObjectMapper objectMapper;

    /**
     * 교수 공유 필기 변경 신호 수신
     *
     * 기존 구조:
     * - WebSocket 메시지를 받으면 SharedStroke를 DB에 다시 저장
     *
     * 변경 구조:
     * - 실제 저장은 REST API(/api/v1/slides/{slideId}/shared-strokes)에서 처리
     * - WebSocket은 학생들에게 "해당 슬라이드의 교수 필기가 변경됨" 신호만 브로드캐스트
     * - 학생 프론트는 이 신호를 받으면 현재 슬라이드 필기 목록을 다시 조회
     */
    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/shared-strokes")
    public void handleSharedStroke(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload SharedStrokeMessage message,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);

        if (user == null) {
            return;
        }

        if (user.getRole() != Role.PROFESSOR) {
            log.warn("WS: 교수가 아닌 사용자의 공유 필기 변경 신호 시도. userId={}", user.getId());
            return;
        }

        SharedStrokeMessage broadcast = SharedStrokeMessage.builder()
                .type("SHARED_STROKE_CHANGED")
                .strokeId(message.getStrokeId())
                .slideId(slideId)
                .strokeSeq(message.getStrokeSeq())
                .stroke(message.getStroke())
                .build();

        messagingTemplate.convertAndSend(
                strokesTopic(spaceId, slideId),
                broadcast
        );

        log.debug("WS: 공유 필기 변경 신호 브로드캐스트. spaceId={}, slideId={}", spaceId, slideId);
    }

    /**
     * 학생이 stroke 수신 확인 ACK 전송
     *
     * payload 예시:
     * {
     *   "lastReceivedStrokeSeq": 25
     * }
     */
    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/ack")
    public void handleAck(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload StrokeAckMessage ackMessage,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);

        if (user == null || ackMessage.getLastReceivedStrokeSeq() == null) {
            return;
        }

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

    /**
     * 학생이 누락된 stroke 재요청
     *
     * payload 예시:
     * {
     *   "lastReceivedStrokeSeq": 24
     * }
     *
     * 현재 구조에서는 Redis에 변경 신호만 저장하지 않으므로,
     * Redis 캐시가 비어 있으면 DB에서 stroke를 조회해 재전송합니다.
     */
    @MessageMapping("/spaces/{spaceId}/slides/{slideId}/resync")
    @Transactional(readOnly = true)
    public void handleResync(
            @DestinationVariable UUID spaceId,
            @DestinationVariable UUID slideId,
            @Payload StrokeResyncRequest request,
            StompHeaderAccessor accessor
    ) {
        User user = resolveUser(accessor, spaceId, slideId);

        if (user == null || request.getLastReceivedStrokeSeq() == null) {
            return;
        }

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
            SharedStrokeMessage resyncMessage = SharedStrokeMessage.builder()
                    .type("SHARED_STROKE_RESYNC")
                    .strokeId(stroke.getStrokeId())
                    .slideId(slideId)
                    .strokeSeq(stroke.getStrokeSeq())
                    .stroke(stroke.getStroke())
                    .build();

            messagingTemplate.convertAndSend(
                    strokesTopic(spaceId, slideId),
                    resyncMessage
            );
        }

        log.debug("WS: 재동기화 완료. slideId={}, 재전송 개수={}", slideId, missing.size());
    }

    private SharedStrokeMessage toResyncMessage(UUID slideId, SharedStroke stroke) {
        return SharedStrokeMessage.builder()
                .type("SHARED_STROKE_RESYNC")
                .strokeId(stroke.getId())
                .slideId(slideId)
                .strokeSeq(stroke.getStrokeSeq())
                .stroke(
                        SharedStrokeMessage.StrokeData.builder()
                                .tool(stroke.getTool())
                                .points(parsePoints(stroke.getPoints()))
                                .color(stroke.getColor())
                                .thickness(stroke.getThickness())
                                .content(stroke.getContent())
                                .strokeOrder(stroke.getStrokeOrder())
                                .build()
                )
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