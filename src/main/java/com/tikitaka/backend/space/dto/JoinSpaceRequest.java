package com.tikitaka.backend.space.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "강의 참여 요청")
public record JoinSpaceRequest(
    @Schema(description = "강의 초대 코드", example = "A1B2C3D4")
    @JsonProperty("space_code")
    @NotBlank(message = "강의 초대 코드는 필수입니다.")
    String spaceCode
) {}
