package com.tikitaka.backend.stroke.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FixerCreateRequest {

    @JsonProperty("x_ratio")
    private Double xRatio;

    @JsonProperty("y_ratio")
    private Double yRatio;

    private String content;
}