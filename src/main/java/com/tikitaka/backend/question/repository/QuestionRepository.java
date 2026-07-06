package com.tikitaka.backend.question.repository;

import com.tikitaka.backend.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findBySlideIdOrderByCreatedAtDesc(UUID slideId);

    List<Question> findBySlideDocumentIdOrderByCreatedAtDesc(UUID documentId);
}