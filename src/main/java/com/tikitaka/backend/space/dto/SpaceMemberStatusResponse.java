package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 참여 상태 변경 응답")
public record SpaceMemberStatusResponse(
    @Schema(description = "강의 멤버 ID", example = "123e4567-e89b-12d3-a456-426614174111")
    @JsonProperty("space_member_id")
    UUID spaceMemberId,

    @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174222")
    @JsonProperty("user_id")
    UUID userId,

    @Schema(description = "참여 상태", example = "APPROVED")
    String validity
) {}
