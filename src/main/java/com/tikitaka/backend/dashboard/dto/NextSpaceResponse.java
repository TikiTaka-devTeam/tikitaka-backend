package com.tikitaka.backend.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class NextSpaceResponse {
    private UUID spaceId;
    private String nickname;
    private String spaceName;
    private String day;
    private LocalTime startTime;
    private String professorName;
    private String semester;
    private long remainTime; // 분 단위
}