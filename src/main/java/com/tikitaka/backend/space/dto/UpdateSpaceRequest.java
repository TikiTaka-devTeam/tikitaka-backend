package com.tikitaka.backend.space.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "강의 정보 수정 요청")
public record UpdateSpaceRequest(
    @Schema(description = "강의명", example = "운영체제")
    String name,

    @Schema(description = "학기", example = "2026-1")
    String semester,

    @Schema(description = "강의 기본 색상", example = "#4F46E5")
    String color,

    @Valid
    @ArraySchema(
        schema = @Schema(implementation = CreateScheduleRequest.class),
        arraySchema = @Schema(description = "강의 시간표 목록")
    )
    List<CreateScheduleRequest> schedules
) {}