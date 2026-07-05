package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Space nickname and personal color update response")
public record UpdateSpaceNicknameResponse(
    @Schema(description = "Space ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "User-specific space nickname", example = "운체")
    String nickname,

    @Schema(description = "User-specific space color", example = "#4F46E5")
    String color
) {}