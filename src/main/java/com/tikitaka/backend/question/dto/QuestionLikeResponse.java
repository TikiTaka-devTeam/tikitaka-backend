package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionLikeResponse {

    @JsonProperty("question_id")
    private UUID questionId;

    @JsonProperty("liked_by_me")
    private Boolean likedByMe;

    @JsonProperty("like_count")
    private Integer likeCount;

    public static QuestionLikeResponse of(UUID questionId, Boolean likedByMe, Integer likeCount) {
        return QuestionLikeResponse.builder()
                .questionId(questionId)
                .likedByMe(likedByMe)
                .likeCount(likeCount)
                .build();
    }
}