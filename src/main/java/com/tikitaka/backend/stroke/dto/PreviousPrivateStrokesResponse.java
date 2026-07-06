package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PreviousPrivateStrokesResponse(
        @JsonProperty("original_slide_id")
        UUID originalSlideId,

        @JsonProperty("background_type")
        String backgroundType,

        String message,

        List<StrokeResponse> strokes
) {
}