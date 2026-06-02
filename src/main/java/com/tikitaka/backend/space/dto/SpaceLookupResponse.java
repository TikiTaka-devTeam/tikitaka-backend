package com.tikitaka.backend.space.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "\ucd08\ub300 \ucf54\ub4dc \uac15\uc758 \uc870\ud68c \uc751\ub2f5")
public record SpaceLookupResponse(
    @Schema(description = "\uac15\uc758 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "\ucd08\ub300 \ucf54\ub4dc", example = "A1B2C3D4")
    @JsonProperty("space_code")
    String spaceCode,

    @Schema(description = "\uac15\uc758\uba85", example = "\uc6b4\uc601\uccb4\uc81c1\ubd84\ubc18(CE)")
    String name,

    @Schema(description = "\uac15\uc758 \ubcc4\uba85", example = "\uc6b4\uccb4")
    String nickname,

    @Schema(description = "\ud559\uae30", example = "2026-1")
    String semester,

    @Schema(description = "\uad50\uc218\uba85", example = "\uae40\uc2b9\ud6c8")
    @JsonProperty("professor_name")
    String professorName,

    @Schema(description = "\uc815\uaddc \uc138\uc158 \ubaa9\ub85d")
    List<SpaceLookupScheduleResponse> schedules
) {}