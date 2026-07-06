package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.entity.Question;

import java.util.UUID;

public record QuestionListResponse(
        @JsonProperty("question_id")
        UUID questionId,

        String content,

        @JsonProperty("refined_content")
        String refinedContent,

        @JsonProperty("like_count")
        Integer likeCount,

        @JsonProperty("liked_by_me")
        Boolean likedByMe,

        @JsonProperty("x_ratio")
        Float xRatio,

        @JsonProperty("y_ratio")
        Float yRatio,

        String status
) {
    public static QuestionListResponse from(Question question) {
        return from(question, false);
    }

    public static QuestionListResponse from(Question question, Boolean likedByMe) {
        return new QuestionListResponse(
                question.getId(),
                question.getContent(),
                question.getRefinedContent(),
                question.getLikeCount(),
                likedByMe,
                question.getXRatio(),
                question.getYRatio(),
                question.getStatus().name()
        );
    }
}