package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "강의 시간표 생성 요청")
public record CreateScheduleRequest(
    @Schema(
        description = "요일",
        example = "MONDAY",
        allowableValues = {
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        }
    )
    @NotBlank(message = "요일은 필수입니다.")
    String day,

    @Schema(description = "강의 시작 시간", example = "09:00")
    @NotNull(message = "시작 시간은 필수입니다.")
    @JsonProperty("start_time")
    String startTime,

    @Schema(description = "강의 종료 시간", example = "10:30")
    @NotNull(message = "종료 시간은 필수입니다.")
    @JsonProperty("end_time")
    String endTime
) {}
