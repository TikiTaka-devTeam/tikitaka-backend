package com.tikitaka.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 수정 요청")
public record UserProfileUpdateRequest(
    @Schema(description = "이름", example = "김선민")
    String name,

    @Schema(description = "전화번호", example = "010-1111-2222")
    @JsonProperty("phone_number")
    String phoneNumber,

    @Schema(description = "전공", example = "컴퓨터공학과")
    String major
) {
}
