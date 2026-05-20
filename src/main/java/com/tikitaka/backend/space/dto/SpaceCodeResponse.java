package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 초대 코드 조회 응답")
public record SpaceCodeResponse(
    @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "강의 초대 코드", example = "A1B2C3D4")
    @JsonProperty("space_code")
    String spaceCode
) {}
