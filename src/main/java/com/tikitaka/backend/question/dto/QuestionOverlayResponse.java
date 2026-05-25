package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionOverlayResponse {

    @JsonProperty("question_id")
    private UUID questionId;

    private String content;

    @JsonProperty("x_ratio")
    private Double xRatio;

    @JsonProperty("y_ratio")
    private Double yRatio;

    @JsonProperty("like_count")
    private Integer likeCount;

    public static QuestionOverlayResponse from(Question question) {
        return QuestionOverlayResponse.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .xRatio(question.getXRatio())
                .yRatio(question.getYRatio())
                .likeCount(question.getLikeCount())
                .build();
    }
}