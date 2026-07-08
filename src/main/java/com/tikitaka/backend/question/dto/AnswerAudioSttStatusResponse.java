package com.tikitaka.backend.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tikitaka.backend.question.audio.AnswerAudioFile;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnswerAudioSttStatusResponse(
        @JsonProperty("audio_file_id")
        UUID audioFileId,

        @JsonProperty("answer_id")
        UUID answerId,

        @JsonProperty("stt_status")
        String sttStatus,

        @JsonProperty("stt_text")
        String sttText,

        @JsonProperty("stt_error_message")
        String sttErrorMessage,

        @JsonProperty("stt_completed_at")
        LocalDateTime sttCompletedAt
) {
    public static AnswerAudioSttStatusResponse from(AnswerAudioFile audioFile) {
        return new AnswerAudioSttStatusResponse(
                audioFile.getId(),
                audioFile.getAnswer().getId(),
                audioFile.getSttStatus(),
                audioFile.getSttText(),
                audioFile.getSttErrorMessage(),
                audioFile.getSttCompletedAt()
        );
    }
}