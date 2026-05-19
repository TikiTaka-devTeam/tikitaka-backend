package com.tikitaka.backend.space.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateSpaceRequest(
    @NotBlank(message = "강의명은 필수입니다.")
    String name,

    @NotBlank(message = "강의 별명은 필수입니다.")
    String nickname,

    @NotBlank(message = "학기는 필수입니다.")
    String semester,

    @NotBlank(message = "강의 색상은 필수입니다.")
    String color,

    @Valid
    @NotEmpty(message = "강의 시간은 최소 1개 이상이어야 합니다.")
    List<CreateScheduleRequest> schedules
) {}
