package com.tikitaka.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUpdateRequest(
    @JsonProperty("object_key")
    @NotBlank(message = "프로필 이미지 object key는 필수입니다")
    String objectKey
) {
}
