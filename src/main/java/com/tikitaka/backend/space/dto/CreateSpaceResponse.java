package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 생성 응답")
public record CreateSpaceResponse(
    @Schema(description = "생성된 강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "강의명", example = "운영체제")
    String name,

    @Schema(description = "강의 별명", example = "운체")
    String nickname,

    @Schema(description = "학기", example = "2026-1")
    String semester,

    @Schema(description = "강의 대표 색상", example = "#4F46E5")
    String color,

    @Schema(description = "학생 참여용 초대 코드", example = "A1B2C3D4")
    @JsonProperty("space_code")
    String spaceCode
) {}
