package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionListResponse {

    @JsonProperty("question_id")
    private UUID questionId;

    private String content;

    @JsonProperty("refined_content")
    private String refinedContent;

    @JsonProperty("like_count")
    private Integer likeCount;

    @JsonProperty("x_ratio")
    private Double xRatio;

    @JsonProperty("y_ratio")
    private Double yRatio;

    private String status;

    public static QuestionListResponse from(Question question) {
        return QuestionListResponse.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .refinedContent(question.getRefinedContent())
                .likeCount(question.getLikeCount())
                .xRatio(question.getXRatio())
                .yRatio(question.getYRatio())
                .status(question.getStatus())
                .build();
    }
}