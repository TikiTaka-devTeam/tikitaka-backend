package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionCreateResponse {

    @JsonProperty("question_id")
    private UUID questionId;

    @JsonProperty("slide_id")
    private UUID slideId;

    private String content;

    private String status;

    @JsonProperty("is_refined")
    private Boolean isRefined;

    public static QuestionCreateResponse from(Question question) {
        return QuestionCreateResponse.builder()
                .questionId(question.getId())
                .slideId(question.getSlide().getId())
                .content(question.getContent())
                .status(question.getStatus().name())
                .isRefined(question.getIsRefined())
                .build();
    }
}