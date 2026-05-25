package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    @JsonProperty("private_stroke_id")
    public UUID getPrivateStrokeId() {
        return privateStrokeId;
    }

    public String getContent() {
        return content;
    }

    @JsonProperty("is_anonymous")
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    @JsonProperty("x_ratio")
    public Float getXRatio() {
        return xRatio;
    }

    @JsonProperty("y_ratio")
    public Float getYRatio() {
        return yRatio;
    }
}