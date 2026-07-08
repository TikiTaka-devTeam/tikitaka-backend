package com.tikitaka.backend.question.repository;

import com.tikitaka.backend.question.audio.AnswerAudioFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerAudioFileRepository extends JpaRepository<AnswerAudioFile, UUID> {
}