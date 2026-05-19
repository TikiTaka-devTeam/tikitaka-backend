package com.tikitaka.backend.space.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "강의 생성 요청")
public record CreateSpaceRequest(
    @Schema(description = "강의명", example = "운영체제")
    @NotBlank(message = "강의명은 필수입니다.")
    String name,

    @Schema(description = "강의 별명", example = "운체")
    @NotBlank(message = "강의 별명은 필수입니다.")
    String nickname,

    @Schema(description = "학기", example = "2026-1")
    @NotBlank(message = "학기는 필수입니다.")
    String semester,

    @Schema(description = "강의 대표 색상", example = "#4F46E5")
    @NotBlank(message = "강의 색상은 필수입니다.")
    String color,

    @Valid
    @NotEmpty(message = "강의 시간은 최소 1개 이상이어야 합니다.")
    @ArraySchema(
        schema = @Schema(implementation = CreateScheduleRequest.class),
        arraySchema = @Schema(description = "강의 시간표 목록")
    )
    List<CreateScheduleRequest> schedules
) {}
