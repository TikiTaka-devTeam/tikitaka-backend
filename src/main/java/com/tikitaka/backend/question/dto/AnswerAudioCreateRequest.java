package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerAudioCreateRequest(
        @JsonProperty("audio_url")
        String audioUrl
) {
}