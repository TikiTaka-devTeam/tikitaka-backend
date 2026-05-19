package com.tikitaka.backend.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class ScheduleResponse {
    private UUID spaceId;
    private String spaceName;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
}