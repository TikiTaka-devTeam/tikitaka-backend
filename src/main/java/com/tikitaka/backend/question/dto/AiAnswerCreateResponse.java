package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.answer.Answer;

import java.util.UUID;

public record AiAnswerCreateResponse(
        @JsonProperty("answer_id")
        UUID answerId,

        @JsonProperty("question_id")
        UUID questionId,

        String content,

        @JsonProperty("answerer_type")
        String answererType,

        @JsonProperty("ai_model")
        String aiModel
) {
    public static AiAnswerCreateResponse from(Answer answer) {
        return new AiAnswerCreateResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getContent(),
                answer.getAnswererType(),
                answer.getAiModel()
        );
    }
}