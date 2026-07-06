package com.tikitaka.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 변경 요청")
public record UserChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "1234")
    @JsonProperty("current_password")
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    String currentPassword,

    @Schema(description = "새 비밀번호", example = "5678")
    @JsonProperty("new_password")
    @NotBlank(message = "새 비밀번호는 필수입니다")
    String newPassword
) {
}
