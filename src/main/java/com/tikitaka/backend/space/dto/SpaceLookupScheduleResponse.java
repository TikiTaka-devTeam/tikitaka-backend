package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "\ucd08\ub300 \ucf54\ub4dc \uac15\uc758 \uc870\ud68c \uc138\uc158 \uc751\ub2f5")
public record SpaceLookupScheduleResponse(
    @Schema(description = "\uc694\uc77c", example = "MONDAY")
    String day,

    @Schema(description = "\uc2dc\uc791 \uc2dc\uac04", example = "10:30")
    @JsonProperty("start_time")
    String startTime,

    @Schema(description = "\uc885\ub8cc \uc2dc\uac04", example = "12:00")
    @JsonProperty("end_time")
    String endTime
) {}