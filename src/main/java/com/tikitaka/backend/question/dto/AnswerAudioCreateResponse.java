package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.audio.AnswerAudioFile;

import java.util.UUID;

public record AnswerAudioCreateResponse(
        @JsonProperty("audio_file_id")
        UUID audioFileId,

        @JsonProperty("answer_id")
        UUID answerId,

        @JsonProperty("audio_url")
        String audioUrl,

        @JsonProperty("stt_status")
        String sttStatus
) {
    public static AnswerAudioCreateResponse from(AnswerAudioFile audioFile) {
        return new AnswerAudioCreateResponse(
                audioFile.getId(),
                audioFile.getAnswer().getId(),
                audioFile.getAudioUrl(),
                audioFile.getSttStatus()
        );
    }
}