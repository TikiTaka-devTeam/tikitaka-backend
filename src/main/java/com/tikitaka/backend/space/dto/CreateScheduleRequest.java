package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateScheduleRequest(
    @NotBlank(message = "요일은 필수입니다.")
    String day,

    @NotNull(message = "시작 시간은 필수입니다.")
    @JsonProperty("start_time")
    String startTime,

    @NotNull(message = "종료 시간은 필수입니다.")
    @JsonProperty("end_time")
    String endTime
) {}
