package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.answer.Answer;
import com.tikitaka.backend.question.entity.Question;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuestionDetailResponse {

    @JsonProperty("question_id")
    private UUID questionId;

    private String content;

    @JsonProperty("refined_content")
    private String refinedContent;

    @JsonProperty("like_count")
    private Integer likeCount;

    private String status;

    private AnswerResponse answer;

    public static QuestionDetailResponse from(Question question, Answer answer) {
        return QuestionDetailResponse.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .refinedContent(question.getRefinedContent())
                .likeCount(question.getLikeCount())
                .status(question.getStatus())
                .answer(answer == null ? null : AnswerResponse.from(answer))
                .build();
    }

    @Getter
    @Builder
    public static class AnswerResponse {

        @JsonProperty("answer_id")
        private UUID answerId;

        @JsonProperty("answerer_type")
        private String answererType;

        private String content;

        @JsonProperty("ai_model")
        private String aiModel;

        public static AnswerResponse from(Answer answer) {
            return AnswerResponse.builder()
                    .answerId(answer.getId())
                    .answererType(answer.getAnswererType())
                    .content(answer.getContent())
                    .aiModel(answer.getAiModel())
                    .build();
        }
    }
}