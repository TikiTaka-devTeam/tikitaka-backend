package com.tikitaka.backend.user.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 조회 응답")
public record UserProfileResponse(
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("user_id")
    UUID userId,

    @Schema(description = "이메일", example = "student@example.com")
    String email,

    @Schema(description = "이름", example = "김선민")
    String name,

    @Schema(description = "역할", example = "STUDENT")
    String role,

    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone_number")
    String phoneNumber,

    @Schema(description = "대학교", example = "단국대학교")
    String univ,

    @Schema(description = "전공", example = "컴퓨터공학과")
    String major,

    @Schema(description = "학번 또는 교번", example = "20231370")
    @JsonProperty("member_id_number")
    String memberIdNumber,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    @JsonProperty("profile_url")
    String profileUrl
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().name(),
            user.getPhoneNumber(),
            user.getUniv(),
            user.getMajor(),
            user.getMemberIdNumber(),
            user.getProfileUrl()
        );
    }
}
