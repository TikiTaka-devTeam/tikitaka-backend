package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.stroke.entity.Fixer;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FixerListResponse {

    @JsonProperty("fixer_id")
    private UUID fixerId;

    @JsonProperty("layer_id")
    private UUID layerId;

    @JsonProperty("x_ratio")
    private Double xRatio;

    @JsonProperty("y_ratio")
    private Double yRatio;

    private String content;

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