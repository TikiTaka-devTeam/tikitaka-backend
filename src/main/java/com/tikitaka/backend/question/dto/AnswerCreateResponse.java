package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.answer.Answer;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AnswerCreateResponse {

    @JsonProperty("answer_id")
    private UUID answerId;

    @JsonProperty("question_id")
    private UUID questionId;

    @JsonProperty("answerer_type")
    private String answererType;

    private String content;

    public static AnswerCreateResponse from(Answer answer) {
        return AnswerCreateResponse.builder()
                .answerId(answer.getId())
                .questionId(answer.getQuestion().getId())
                .answererType(answer.getAnswererType())
                .content(answer.getContent())
                .build();
    }
}