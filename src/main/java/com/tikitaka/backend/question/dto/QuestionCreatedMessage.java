package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionCreatedMessage {

    private String type;

    @JsonProperty("slide_id")
    private UUID slideId;

    @JsonProperty("question_id")
    private UUID questionId;

    private QuestionListResponse question;
}