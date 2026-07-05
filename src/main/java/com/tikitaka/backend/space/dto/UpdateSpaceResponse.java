package com.tikitaka.backend.space.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 정보 수정 응답")
public record UpdateSpaceResponse(
    @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "강의명", example = "운영체제")
    String name,

    @Schema(description = "학기", example = "2026-1")
    String semester,

    @Schema(description = "강의 기본 색상", example = "#4F46E5")
    String color,

    @ArraySchema(
        schema = @Schema(implementation = SpaceLookupScheduleResponse.class),
        arraySchema = @Schema(description = "강의 시간표 목록")
    )
    List<SpaceLookupScheduleResponse> schedules
) {}