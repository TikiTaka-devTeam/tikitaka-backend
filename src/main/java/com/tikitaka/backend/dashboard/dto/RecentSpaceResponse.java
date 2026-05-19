package com.tikitaka.backend.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecentSpaceResponse {
    private UUID spaceId;
    private String name;
    private String nickname;
    private String color;
    private String professorName;
    private OffsetDateTime lastAccessedAt;
}