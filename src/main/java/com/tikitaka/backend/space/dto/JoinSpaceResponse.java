package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 참여 요청 응답")
public record JoinSpaceResponse(
    @Schema(description = "강의 멤버 ID", example = "123e4567-e89b-12d3-a456-426614174111")
    @JsonProperty("space_member_id")
    UUID spaceMemberId,

    @Schema(description = "강의 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("space_id")
    UUID spaceId,

    @Schema(description = "강의명", example = "운영체제")
    @JsonProperty("space_name")
    String spaceName,

    @Schema(description = "참여 상태", example = "PENDING")
    String validity
) {}
