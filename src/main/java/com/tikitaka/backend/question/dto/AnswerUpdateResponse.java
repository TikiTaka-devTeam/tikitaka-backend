package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.answer.Answer;

import java.util.UUID;

public record AnswerUpdateResponse(
        @JsonProperty("answer_id")
        UUID answerId,

        @JsonProperty("question_id")
        UUID questionId,

        String content,

        @JsonProperty("answerer_type")
        String answererType
) {
    public static AnswerUpdateResponse from(Answer answer) {
        return new AnswerUpdateResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getContent(),
                answer.getAnswererType()
        );
    }
}