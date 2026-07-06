package com.tikitaka.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청")
public record UserChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "12345678")
    @JsonProperty("current_password")
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    String currentPassword,

    @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다")
    @Schema(description = "새 비밀번호", example = "876543421")
    @JsonProperty("new_password")
    @NotBlank(message = "새 비밀번호는 필수입니다")
    String newPassword
) {
}
