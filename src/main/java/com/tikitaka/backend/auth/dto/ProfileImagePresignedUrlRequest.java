package com.tikitaka.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record ProfileImagePresignedUrlRequest(
    @JsonProperty("original_filename")
    @NotBlank(message = "파일 이름은 필수입니다")
    String originalFilename,

    @JsonProperty("content_type")
    @NotBlank(message = "Content-Type은 필수입니다")
    String contentType
) {
}
