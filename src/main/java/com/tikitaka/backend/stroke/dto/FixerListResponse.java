package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.stroke.entity.Fixer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "수정 메모 목록 조회 응답")
public class FixerListResponse {

    @Schema(description = "수정 메모 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("fixer_id")
    private UUID fixerId;

    @Schema(description = "교수 개인 레이어 ID", example = "550e8400-e29b-41d4-a716-446655440002")
    @JsonProperty("layer_id")
    private UUID layerId;

    @Schema(description = "슬라이드 내 X 좌표 비율", example = "0.35")
    @JsonProperty("x_ratio")
    private Double xRatio;

    @Schema(description = "슬라이드 내 Y 좌표 비율", example = "0.62")
    @JsonProperty("y_ratio")
    private Double yRatio;

    @Schema(description = "수정 메모 내용", example = "이 부분 수식 수정 필요")
    private String content;

    @Schema(description = "수정 메모 완료 여부", example = "false")
    @JsonProperty("is_checked")
    private Boolean isChecked;

    public static FixerListResponse from(Fixer fixer) {
        return FixerListResponse.builder()
                .fixerId(fixer.getId())
                .layerId(fixer.getLayer().getId())
                .xRatio(fixer.getXRatio())
                .yRatio(fixer.getYRatio())
                .content(fixer.getContent())
                .isChecked(fixer.getIsChecked())
                .build();
    }
}