package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.stroke.entity.Fixer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "수정 메모 완료 처리 응답")
public class FixerCheckResponse {

    @Schema(description = "수정 메모 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("fixer_id")
    private UUID fixerId;

    @Schema(description = "수정 메모 완료 여부", example = "true")
    @JsonProperty("is_checked")
    private Boolean isChecked;

    public static FixerCheckResponse from(Fixer fixer) {
        return FixerCheckResponse.builder()
                .fixerId(fixer.getId())
                .isChecked(fixer.getIsChecked())
                .build();
    }
}