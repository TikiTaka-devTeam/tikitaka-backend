package com.tikitaka.backend.stroke.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikitaka.backend.stroke.dto.SharedStrokeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrokeRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long TTL_HOURS = 24;

    public void saveStroke(UUID slideId, int strokeSeq, SharedStrokeMessage message) {
        String key = strokesKey(slideId);
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForZSet().add(key, json, strokeSeq);
            redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis stroke 저장 실패: slideId={}, strokeSeq={}", slideId, strokeSeq, e);
        }
    }

    // lastReceivedStrokeSeq 이후의 stroke 목록 반환 (Redis 캐시 우선, 없으면 빈 리스트)
    public List<SharedStrokeMessage> getStrokesAfter(UUID slideId, int lastReceivedStrokeSeq) {
        String key = strokesKey(slideId);
        try {
            Set<String> members = redisTemplate.opsForZSet()
                    .rangeByScore(key, lastReceivedStrokeSeq + 1, Double.MAX_VALUE);
            if (members == null || members.isEmpty()) return List.of();
            return members.stream()
                    .map(this::parseMessage)
                    .filter(m -> m != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Redis stroke 조회 실패: slideId={}", slideId, e);
            return List.of();
        }
    }

    public void saveAck(UUID userId, UUID slideId, int lastReceivedStrokeSeq) {
        String key = "student:" + userId + ":slide:" + slideId + ":ack";
        try {
            redisTemplate.opsForValue().set(key, String.valueOf(lastReceivedStrokeSeq), TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis ACK 저장 실패: userId={}, slideId={}", userId, slideId, e);
        }
    }

    private String strokesKey(UUID slideId) {
        return "slide:" + slideId + ":strokes";
    }

    private SharedStrokeMessage parseMessage(String json) {
        try {
            return objectMapper.readValue(json, SharedStrokeMessage.class);
        } catch (Exception e) {
            log.warn("Redis stroke JSON 파싱 실패", e);
            return null;
        }
    }
}
