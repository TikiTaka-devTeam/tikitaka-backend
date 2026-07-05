package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.stroke.entity.Fixer;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FixerCreateResponse {

    @JsonProperty("fixer_id")
    private UUID fixerId;

    @JsonProperty("slide_id")
    private UUID slideId;

    @JsonProperty("layer_id")
    private UUID layerId;

    @JsonProperty("x_ratio")
    private Double xRatio;

    @JsonProperty("y_ratio")
    private Double yRatio;

    private String content;

    @JsonProperty("is_checked")
    private Boolean isChecked;

    public static FixerCreateResponse from(Fixer fixer) {
        return FixerCreateResponse.builder()
                .fixerId(fixer.getId())
                .slideId(fixer.getLayer().getSlide().getId())
                .layerId(fixer.getLayer().getId())
                .xRatio(fixer.getXRatio())
                .yRatio(fixer.getYRatio())
                .content(fixer.getContent())
                .isChecked(fixer.getIsChecked())
                .build();
    }
}