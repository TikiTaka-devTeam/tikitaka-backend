package com.tikitaka.backend.space.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강의 참여자 요약 응답")
public record SpaceMemberSummaryResponse(
    @Schema(description = "강의 멤버 ID", example = "123e4567-e89b-12d3-a456-426614174111")
    @JsonProperty("space_member_id")
    UUID spaceMemberId,

    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174222")
    @JsonProperty("user_id")
    UUID userId,

    @Schema(description = "사용자 이름", example = "김선민")
    String name,

    @Schema(description = "학번", example = "20231370")
    @JsonProperty("member_id_number")
    String memberIdNumber
) {}
