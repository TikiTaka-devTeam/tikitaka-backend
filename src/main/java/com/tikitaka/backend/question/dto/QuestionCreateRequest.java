package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class QuestionCreateRequest {

    @JsonProperty("private_stroke_id")
    private UUID privateStrokeId;

    private String content;

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous;

    @JsonProperty("x_ratio")
    private Float xRatio;

    @JsonProperty("y_ratio")
    private Float yRatio;
}