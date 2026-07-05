package com.tikitaka.backend.space.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Space nickname and personal color update request")
public record UpdateSpaceNicknameRequest(
    @Schema(description = "User-specific space nickname", example = "운체")
    String nickname,

    @Schema(description = "User-specific space color", example = "#4F46E5")
    String color
) {}